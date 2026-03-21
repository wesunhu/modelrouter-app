/**
 * OpenAI-compatible REST: POST /v1/chat/completions, GET /v1/models; input: JSON body, Bearer route key.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelrouter.client.ChatParams;
import com.modelrouter.service.RouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAI 兼容 API：/v1/chat/completions, /v1/models
 * 同时支持 /api/v1/* 路径（部分客户端 base URL 为 /api 时使用）
 * 参考 https://docs.ollama.com/api/openai-compatibility
 */
@RestController
@RequestMapping({"/v1", "/api/v1"})
public class OpenAIController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RouterService routerService;

    @Autowired
    private com.modelrouter.service.RouteService routeService;

    @PostMapping(value = {"/chat/completions", "/chat/completions/"})
    public ResponseEntity<?> chatCompletions(
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        String model = (String) body.get("model");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        Boolean stream = Boolean.TRUE.equals(body.get("stream"));

        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", Map.of(
                    "message", "messages is required",
                    "type", "invalid_request_error",
                    "code", "invalid_request_error"
            )));
        }

        String token = authorization != null ? authorization : "";
        ChatParams params = ChatParams.from(body);
        Map<String, Object> response = routerService.routeByModelAndToken(model, token, messages, params);

        if (stream) {
            return streamResponse(response);
        }
        return ResponseEntity.ok(response);
    }

    /** 将完整响应格式化为 SSE 流（兼容 stream=true 的客户端） */
    private ResponseEntity<?> streamResponse(Map<String, Object> resp) {
        try {
            String id = (String) resp.get("id");
            String model = (String) resp.get("model");
            if (id == null || id.isBlank()) id = "chatcmpl-" + System.currentTimeMillis();
            if (model == null || model.isBlank()) model = "";
            Object choices = resp.get("choices");
            String content = "";
            String finishReason = "stop";
            if (choices instanceof List<?> list && !list.isEmpty()) {
                Object c0 = list.get(0);
                if (c0 instanceof Map<?, ?> choice) {
                    Object msg = choice.get("message");
                    if (msg instanceof Map<?, ?> m && m.containsKey("content")) {
                        Object ct = m.get("content");
                        content = ct != null ? ct.toString() : "";
                    }
                    if (choice.containsKey("finish_reason")) {
                        Object fr = choice.get("finish_reason");
                        finishReason = fr != null ? fr.toString() : "stop";
                    }
                }
            }
            // SSE 格式：delta 块 + 结束块（Map.of 不支持 null，streaming 首块 finish_reason 需为 null）
            Map<String, Object> delta1 = Map.of("content", content != null ? content : "");
            Map<String, Object> choice1 = new java.util.HashMap<>();
            choice1.put("index", 0);
            choice1.put("delta", delta1);
            choice1.put("finish_reason", null);
            Map<String, Object> chunk1Map = Map.of(
                    "id", id, "object", "chat.completion.chunk", "created", System.currentTimeMillis() / 1000,
                    "model", model != null ? model : "", "choices", List.of(choice1)
            );
            Map<String, Object> chunk2Map = new java.util.HashMap<>();
            chunk2Map.put("id", id);
            chunk2Map.put("object", "chat.completion.chunk");
            chunk2Map.put("created", System.currentTimeMillis() / 1000);
            chunk2Map.put("model", model);
            chunk2Map.put("choices", List.of(Map.of("index", 0, "delta", Map.of(), "finish_reason", finishReason)));
            if (resp.containsKey("usage")) chunk2Map.put("usage", resp.get("usage"));

            String chunk1 = "data: " + objectMapper.writeValueAsString(chunk1Map) + "\n\n";
            String chunk2 = "data: " + objectMapper.writeValueAsString(chunk2Map) + "\n\n";
            String chunk3 = "data: [DONE]\n\n";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/event-stream"));
            headers.set("Cache-Control", "no-cache");
            headers.set("Connection", "keep-alive");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(chunk1 + chunk2 + chunk3);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", Map.of("message", e.getMessage())));
        }
    }

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        List<Map<String, Object>> data = routeService.findByStatus("active").stream()
                .map(this::toRouteModelDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "object", "list",
                "data", data
        ));
    }

    @GetMapping("/models/{modelId}")
    public ResponseEntity<?> getModel(@PathVariable String modelId) {
        // 优先按路由 api_key 或 name 匹配
        var routeMatch = routeService.findByStatus("active").stream()
                .filter(r -> (r.getName() != null && r.getName().equalsIgnoreCase(modelId))
                        || (r.getApiKey() != null && r.getApiKey().equals(modelId)))
                .findFirst();
        if (routeMatch.isPresent()) {
            return ResponseEntity.ok(toRouteModelDto(routeMatch.get()));
        }
        return ResponseEntity.status(404).body(Map.of("error", Map.of(
                "message", "The model '" + modelId + "' does not exist",
                "type", "invalid_request_error",
                "code", "model_not_found"
        )));
    }

    /** 返回路由作为 model：id 使用路由名称（router 表中的 name） */
    private Map<String, Object> toRouteModelDto(com.modelrouter.entity.Route r) {
        return Map.of(
                "id", r.getName(),
                "object", "model",
                "created", System.currentTimeMillis() / 1000,
                "owned_by", r.getName()
        );
    }
}
