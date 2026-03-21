/**
 * Core chat routing: selects route, calls ModelApiClient, failover, usage logging.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.service;

import com.modelrouter.entity.*;
import com.modelrouter.repository.*;
import com.modelrouter.client.ModelApiClient;
import com.modelrouter.client.ChatParams;
import com.modelrouter.exception.InvalidApiKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能路由服务：选择模型、故障转移、记录使用日志
 */
@Service
public class RouterService {

    private static final Logger log = LoggerFactory.getLogger(RouterService.class);

    @Autowired
    private ModelService modelService;
    @Autowired
    private ApiKeyService apiKeyService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private UsageLogRepository usageLogRepository;
    @Autowired
    private ModelApiClient modelApiClient;

    /**
     * 通过 OpenAI 兼容接口调用：根据 model 名称和 Bearer token 路由
     */
    public Map<String, Object> routeByModelAndToken(String requestedModel, String bearerToken,
                                                    List<Map<String, Object>> messages,
                                                    Double temperature, Integer maxTokens) {
        return routeByModelAndToken(requestedModel, bearerToken, messages,
                new ChatParams(temperature, maxTokens, null, null, null, null, null, null));
    }

    /**
     * 通过 OpenAI 兼容接口调用，支持完整参数（top_p、stop 等）
     * Token 可匹配：API Key 管理中的 key/secret，或路由配置中的 api_key
     */
    public Map<String, Object> routeByModelAndToken(String requestedModel, String bearerToken,
                                                    List<Map<String, Object>> messages,
                                                    ChatParams params) {
        String token = extractToken(bearerToken);
        if (token == null || token.isEmpty()) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        // 1. 优先匹配路由的 api_key：使用该路由的模型列表
        Route route = routeService.findByApiKey(token).orElse(null);
        if (route != null) {
            return routeByRouteWithModelFilter(route, requestedModel, messages, params);
        }

        // 2. 匹配 API Key 管理中的 key 或 secret
        ApiKey apiKeyEntity = resolveApiKey(bearerToken);
        if (apiKeyEntity == null) {
            throw new InvalidApiKeyException("Invalid API key");
        }

        List<Model> candidates = findModelsForRequest(requestedModel, apiKeyEntity);
        if (candidates.isEmpty()) {
            throw new RuntimeException("No available model for: " + requestedModel);
        }

        ChatParams p = params != null ? params : new ChatParams(null, null, null, null, null, null, null, null);

        Exception lastError = null;
        for (Model model : candidates) {
            try {
                Map<String, Object> response = modelApiClient.chat(model, messages, p);
                recordUsage(model, null, apiKeyEntity.getId(), response);
                return response;
            } catch (Exception e) {
                lastError = e;
                log.warn("Model {} failed: {}, switching to next", model.getName(), e.getMessage());
            }
        }
        throw new RuntimeException("All models failed: " + (lastError != null ? lastError.getMessage() : ""));
    }

    /** 使用路由的模型列表，按 requestedModel 过滤后调用
     * 当 requestedModel 为路由名称或 api_key 时，视为请求该路由，不按模型名过滤 */
    private Map<String, Object> routeByRouteWithModelFilter(Route route, String requestedModel,
                                                            List<Map<String, Object>> messages,
                                                            ChatParams params) {
        List<Model> candidates = buildModelCandidates(route);
        boolean isRouteIdentifier = requestedModel != null && !requestedModel.isBlank()
                && (route.getName() != null && requestedModel.equalsIgnoreCase(route.getName())
                || (route.getApiKey() != null && requestedModel.equals(route.getApiKey())));
        if (!isRouteIdentifier && requestedModel != null && !requestedModel.isBlank()) {
            candidates = candidates.stream()
                    .filter(m -> matchesModel(m, requestedModel))
                    .collect(Collectors.toList());
        }
        if (candidates.isEmpty()) {
            throw new RuntimeException("No available model for: " + (requestedModel != null ? requestedModel : "this route"));
        }
        ChatParams p = params != null ? params : new ChatParams(null, null, null, null, null, null, null, null);
        Integer timeoutMs = route.getTimeout();
        Exception lastError = null;
        for (Model model : candidates) {
            try {
                Map<String, Object> response = modelApiClient.chat(model, messages, p, timeoutMs);
                recordUsage(model, route, null, response);
                return response;
            } catch (Exception e) {
                lastError = e;
                log.warn("Model {} failed: {}, switching to next", model.getName(), e.getMessage());
            }
        }
        throw new RuntimeException("All models failed: " + (lastError != null ? lastError.getMessage() : ""));
    }

    private String extractToken(String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) return null;
        String t = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7).trim() : bearerToken.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 通过 Route 调用：根据 Route 的 apiKey 和 primary/backup 模型
     * 返回包含 response 和 router_log 的 Map，便于前端展示路由尝试过程
     */
    public Map<String, Object> routeByRoute(Route route, List<Map<String, Object>> messages,
                                            Double temperature, Integer maxTokens) {
        List<Model> candidates = buildModelCandidates(route);
        List<Map<String, Object>> attempts = new ArrayList<>();

        if (candidates.isEmpty()) {
            attempts.add(Map.of(
                    "event", "no_models",
                    "message", "No models configured for route: " + route.getName()
            ));
            throw new RouterException("No models configured for route: " + route.getName() + ". Check primary model and backup models are set and active.", attempts);
        }

        Exception lastError = null;
        int index = 0;

        for (Model model : candidates) {
            index++;
            String modelName = model.getName();
            String providerName = model.getProvider() != null ? model.getProvider().getName() : "unknown";
            attempts.add(Map.of(
                    "event", "trying",
                    "index", index,
                    "total", candidates.size(),
                    "model", modelName,
                    "provider", providerName
            ));
            try {
                log.info("Trying model #{}/{}: {} (provider: {})", index, candidates.size(), modelName, providerName);
                Map<String, Object> response = modelApiClient.chat(model, messages, temperature, maxTokens, route.getTimeout());
                recordUsage(model, route, null, response);
                log.info("Model {} succeeded", modelName);
                attempts.set(attempts.size() - 1, Map.of(
                        "event", "succeeded",
                        "index", index,
                        "total", candidates.size(),
                        "model", modelName,
                        "provider", providerName
                ));
                Map<String, Object> result = new HashMap<>(response);
                result.put("router_log", attempts);
                return result;
            } catch (Exception e) {
                lastError = e;
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                attempts.set(attempts.size() - 1, Map.of(
                        "event", "failed",
                        "index", index,
                        "total", candidates.size(),
                        "model", modelName,
                        "provider", providerName,
                        "error", msg
                ));
                log.warn("Model {} failed (401/404/429/500 or other): {}, switching to next", modelName, msg);
            }
        }
        String detail = lastError != null
                ? (lastError.getCause() != null ? lastError.getCause().getMessage() : lastError.getMessage())
                : "Unknown";
        if (detail == null) detail = lastError != null ? lastError.getClass().getSimpleName() : "Unknown";
        attempts.add(Map.of("event", "all_failed", "message", detail));
        log.error("All models failed for route: {}. Last error: {}", route.getName(), detail, lastError);
        throw new RouterException("All models failed for route: " + route.getName() + ". Last error: " + detail, attempts);
    }

    /** 携带 router_log 的路由异常 */
    public static class RouterException extends RuntimeException {
        private final List<Map<String, Object>> attempts;
        public RouterException(String msg, List<Map<String, Object>> attempts) {
            super(msg);
            this.attempts = attempts;
        }
        public List<Map<String, Object>> getAttempts() { return attempts; }
    }

    private ApiKey resolveApiKey(String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) return null;
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7).trim() : bearerToken.trim();
        return apiKeyService.findByKey(token)
                .or(() -> apiKeyService.findBySecret(token))
                .orElse(null);
    }

    private List<Model> findModelsForRequest(String requestedModel, ApiKey apiKey) {
        List<Model> all = modelService.findByStatus("active");
        List<Model> allowed = all.stream()
                .filter(m -> apiKey.hasAccessToModel(m.getId()))
                .collect(Collectors.toList());
        if (allowed.isEmpty()) allowed = all;

        return allowed.stream()
                .filter(m -> matchesModel(m, requestedModel))
                .collect(Collectors.toList());
    }

    private boolean matchesModel(Model m, String requested) {
        if (requested == null) return true;
        String r = requested.toLowerCase();
        return m.getName().toLowerCase().contains(r)
                || m.getModelId().toLowerCase().contains(r)
                || r.contains(m.getName().toLowerCase())
                || r.contains(m.getModelId().toLowerCase());
    }

    private List<Model> buildModelCandidates(Route route) {
        // 主模型 → 备用1 → 备用2，遇 401/404/429/500 等错误立即切换下一模型
        List<Long> ids = new ArrayList<>();
        if (route.getPrimaryModelId() != null) ids.add(route.getPrimaryModelId());
        if (route.getBackupModelIds() != null && !route.getBackupModelIds().isEmpty()) {
            ids.addAll(route.getBackupModelIds());
        }
        if (ids.isEmpty()) return List.of();
        // 一次批量加载，避免 N 次 findById（含缓存）
        Map<Long, Model> modelMap = modelService.findAllById(ids).stream()
                .filter(m -> "active".equals(m.getStatus()))
                .collect(Collectors.toMap(Model::getId, m -> m, (a, b) -> a));
        // 按主模型→备选顺序返回
        List<Model> result = new ArrayList<>();
        for (Long id : ids) {
            Model m = modelMap.get(id);
            if (m != null) result.add(m);
        }
        return result;
    }

    private void recordUsage(Model model, Route route, Long apiKeyId, Map<String, Object> response) {
        UsageLog log = new UsageLog();
        log.setModelId(model.getId());
        log.setModelName(model.getName());
        log.setPlatform(model.getProvider().getName());
        log.setApiKeyId(apiKeyId);

        if (route != null) {
            log.setRouteId(route.getId());
            log.setRouteName(route.getName());
        }

        Map<?, ?> usage = (Map<?, ?>) response.get("usage");
        if (usage != null) {
            log.setPromptTokens(getInt(usage, "prompt_tokens", "input_tokens"));
            log.setCompletionTokens(getInt(usage, "completion_tokens", "output_tokens"));
            log.setTotalTokens(getInt(usage, "total_tokens", null));
        }

        int totalTk = log.getTotalTokens() != null ? log.getTotalTokens() : 0;
        int pt = log.getPromptTokens() != null ? log.getPromptTokens() : 0;
        int ct = log.getCompletionTokens() != null ? log.getCompletionTokens() : 0;
        if (totalTk == 0 && (pt > 0 || ct > 0)) {
            totalTk = pt + ct;
            log.setTotalTokens(totalTk);
        }

        double cost = 0;
        Double costPerToken = null;
        if (model.getTokenCost() != null && model.getTokenCost() > 0) {
            costPerToken = model.getTokenCost();
            cost = totalTk * costPerToken;
        } else if (model.getCostInput() != null && model.getCostOutput() != null) {
            cost = pt * model.getCostInput() + ct * model.getCostOutput();
            if (totalTk > 0) costPerToken = cost / totalTk;
        }
        log.setCost(cost);
        log.setCostPerToken(costPerToken);

        if (route != null && route.getTokenSellingPrice() != null) {
            log.setSellingPricePerToken(route.getTokenSellingPrice());
        }

        usageLogRepository.save(log);
    }

    private int getInt(Map<?, ?> m, String key1, String key2) {
        Object v = m.get(key1);
        if (v == null && key2 != null) v = m.get(key2);
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
