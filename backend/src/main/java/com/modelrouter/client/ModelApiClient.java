/**
 * HTTP client to upstream providers; input: chat params; output: provider response body.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.modelrouter.entity.Model;
import com.modelrouter.entity.Provider;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 真实调用各平台 API 的 HTTP 客户端。使用 WebClient 支持异步与 per-request 超时。
 */
@Component
public class ModelApiClient {

    /** 默认读取超时（ms），路由未指定时使用 */
    private static final int DEFAULT_READ_TIMEOUT_MS = 120_000;

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ModelApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 调用模型 API，使用默认超时。
     */
    public Map<String, Object> chat(Model model, List<Map<String, Object>> messages,
                                    Double temperature, Integer maxTokens) throws Exception {
        return chat(model, messages, temperature, maxTokens, null);
    }

    /**
     * 调用模型 API。timeoutMs 为 null 时使用默认 120 秒。
     */
    public Map<String, Object> chat(Model model, List<Map<String, Object>> messages,
                                    Double temperature, Integer maxTokens, Integer timeoutMs) throws Exception {
        return chat(model, messages, new ChatParams(temperature, maxTokens, null, null, null, null, null, null), timeoutMs);
    }

    /**
     * 调用模型 API，支持完整参数，使用默认超时。
     */
    public Map<String, Object> chat(Model model, List<Map<String, Object>> messages, ChatParams params) throws Exception {
        return chat(model, messages, params, null);
    }

    /**
     * 调用模型 API。timeoutMs 为 null 时使用默认 120 秒。Route 模式下使用 route.getTimeout()。
     */
    public Map<String, Object> chat(Model model, List<Map<String, Object>> messages, ChatParams params, Integer timeoutMs) throws Exception {
        int readTimeout = (timeoutMs != null && timeoutMs > 0) ? timeoutMs : DEFAULT_READ_TIMEOUT_MS;
        Provider provider = model.getProvider();
        if (provider == null) {
            throw new RuntimeException("Model has no provider configured");
        }
        String platform = provider.getName() != null ? provider.getName().toLowerCase() : "openai";
        String baseUrl = provider.getBaseUrl() != null ? provider.getBaseUrl() : "";
        String platformKey = provider.getApiKey();
        if ((platformKey == null || platformKey.isBlank()) &&
                ("openai".equals(platform) || "openai_compatible".equals(platform) || "anthropic".equals(platform) || "google".equals(platform))) {
            throw new RuntimeException("Provider has no API Key. Set it in Providers.");
        }
        String token = (platformKey != null && !platformKey.isBlank()) ? platformKey : "";

        switch (platform) {
            case "openai":
            case "openai_compatible":
                return callOpenAI(baseUrl, model.getModelId(), token, messages, params, readTimeout);
            case "anthropic":
                return callAnthropic(baseUrl, model.getModelId(), token, messages,
                        params != null ? params.temperature() : null, params != null ? params.maxTokens() : null, readTimeout);
            case "google":
                return callGoogle(baseUrl, model.getModelId(), token, messages,
                        params != null ? params.temperature() : null, params != null ? params.maxTokens() : null, readTimeout);
            default:
                return callOpenAI(baseUrl, model.getModelId(), token, messages, params, readTimeout);
        }
    }

    private Map<String, Object> callOpenAI(String baseUrl, String modelId, String apiKey,
                                           List<Map<String, Object>> messages,
                                           ChatParams params, int readTimeoutMs) throws Exception {
        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";
        Double temperature = params != null ? params.temperature() : null;
        Integer maxTokens = params != null ? params.maxTokens() : null;

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", modelId);
        body.set("messages", objectMapper.valueToTree(messages));
        if (temperature != null) body.put("temperature", temperature);
        if (maxTokens != null) body.put("max_tokens", maxTokens);
        if (params != null) {
            if (params.topP() != null) body.put("top_p", params.topP());
            if (params.stop() != null) body.set("stop", objectMapper.valueToTree(params.stop()));
            if (params.presencePenalty() != null) body.put("presence_penalty", params.presencePenalty());
            if (params.frequencyPenalty() != null) body.put("frequency_penalty", params.frequencyPenalty());
            if (params.seed() != null) body.put("seed", params.seed());
            if (params.responseFormat() != null) body.set("response_format", objectMapper.valueToTree(params.responseFormat()));
        }

        String json = post(url, apiKey, body, readTimeoutMs);
        return objectMapper.readValue(json, Map.class);
    }

    private Map<String, Object> callAnthropic(String baseUrl, String modelId, String apiKey,
                                              List<Map<String, Object>> messages,
                                              Double temperature, Integer maxTokens, int readTimeoutMs) throws Exception {
        String url = baseUrl.replaceAll("/+$", "") + "/v1/messages";

        ArrayNode anthMessages = objectMapper.createArrayNode();
        for (Map<String, Object> m : messages) {
            ObjectNode msg = objectMapper.createObjectNode();
            msg.put("role", m.get("role").toString());
            msg.put("content", m.get("content").toString());
            anthMessages.add(msg);
        }

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", modelId);
        body.put("max_tokens", maxTokens != null ? maxTokens : 2048);
        if (temperature != null) body.put("temperature", temperature);
        body.set("messages", anthMessages);

        String json = postAnth(url, apiKey, body, readTimeoutMs);
        JsonNode root = objectMapper.readTree(json);
        return convertAnthropicToOpenAI(root, modelId);
    }

    private Map<String, Object> convertAnthropicToOpenAI(JsonNode root, String modelId) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "chatcmpl-" + System.currentTimeMillis());
        result.put("object", "chat.completion");
        result.put("created", System.currentTimeMillis() / 1000);
        result.put("model", modelId);

        ArrayNode choices = objectMapper.createArrayNode();
        ObjectNode choice = objectMapper.createObjectNode();
        choice.put("index", 0);
        ObjectNode msg = objectMapper.createObjectNode();
        JsonNode content = root.path("content");
        if (content.isArray() && content.size() > 0) {
            msg.put("content", content.get(0).path("text").asText());
        } else {
            msg.put("content", "");
        }
        msg.put("role", "assistant");
        choice.set("message", msg);
        choice.put("finish_reason", "stop");
        choices.add(choice);
        result.put("choices", choices);

        Map<String, Object> usage = new HashMap<>();
        JsonNode u = root.path("usage");
        usage.put("prompt_tokens", u.path("input_tokens").asInt(0));
        usage.put("completion_tokens", u.path("output_tokens").asInt(0));
        usage.put("total_tokens", u.path("input_tokens").asInt(0) + u.path("output_tokens").asInt(0));
        result.put("usage", usage);

        return result;
    }

    private Map<String, Object> callGoogle(String baseUrl, String modelId, String apiKey,
                                           List<Map<String, Object>> messages,
                                           Double temperature, Integer maxTokens, int readTimeoutMs) throws Exception {
        String url = baseUrl.replaceAll("/+$", "") + "/v1beta/models/" + modelId + ":generateContent";

        ArrayNode contents = objectMapper.createArrayNode();
        for (Map<String, Object> m : messages) {
            ObjectNode part = objectMapper.createObjectNode();
            part.put("role", m.get("role").toString());
            ObjectNode textPart = objectMapper.createObjectNode();
            textPart.put("text", m.get("content").toString());
            part.set("parts", objectMapper.createArrayNode().add(textPart));
            contents.add(part);
        }

        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode genConfig = objectMapper.createObjectNode();
        if (maxTokens != null) genConfig.put("maxOutputTokens", maxTokens);
        if (temperature != null) genConfig.put("temperature", temperature);
        body.set("contents", contents);
        body.set("generationConfig", genConfig);

        String json = post(url, apiKey, body, readTimeoutMs);
        JsonNode root = objectMapper.readTree(json);
        return convertGoogleToOpenAI(root, modelId);
    }

    private Map<String, Object> convertGoogleToOpenAI(JsonNode root, String modelId) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "chatcmpl-" + System.currentTimeMillis());
        result.put("object", "chat.completion");
        result.put("created", System.currentTimeMillis() / 1000);
        result.put("model", modelId);

        ArrayNode choices = objectMapper.createArrayNode();
        ObjectNode choice = objectMapper.createObjectNode();
        choice.put("index", 0);
        ObjectNode msg = objectMapper.createObjectNode();
        JsonNode candidates = root.path("candidates");
        String content = "";
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode c0 = candidates.get(0).path("content").path("parts");
            if (c0.isArray() && c0.size() > 0) {
                content = c0.get(0).path("text").asText();
            }
        }
        msg.put("content", content);
        msg.put("role", "assistant");
        choice.set("message", msg);
        choice.put("finish_reason", "stop");
        choices.add(choice);
        result.put("choices", choices);

        Map<String, Object> usage = new HashMap<>();
        JsonNode u = root.path("usageMetadata");
        int pt = u.path("promptTokenCount").asInt(0);
        int ct = u.path("candidatesTokenCount").asInt(0);
        usage.put("prompt_tokens", pt);
        usage.put("completion_tokens", ct);
        usage.put("total_tokens", pt + ct);
        result.put("usage", usage);

        return result;
    }

    private String post(String url, String apiKey, ObjectNode body, int readTimeoutMs) throws Exception {
        var spec = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body.toString());
        if (apiKey != null && !apiKey.isEmpty()) {
            spec = spec.header("Authorization", "Bearer " + apiKey);
        }
        try {
            return spec.retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(readTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("HTTP " + e.getStatusCode().value() + ": " + (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage()));
        }
    }

    private String postAnth(String url, String apiKey, ObjectNode body, int readTimeoutMs) throws Exception {
        try {
            return webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("x-api-key", apiKey != null ? apiKey : "")
                    .header("anthropic-version", "2023-06-01")
                    .bodyValue(body.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(readTimeoutMs))
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("HTTP " + e.getStatusCode().value() + ": " + (e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : e.getMessage()));
        }
    }
}
