/**
 * POST /api/test/chat for admin UI model testing; input: chat request JSON.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.controller;

import com.modelrouter.entity.Route;
import com.modelrouter.service.RouteService;
import com.modelrouter.service.RouterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 模型测试接口：使用路由配置（含路由的 API Key）进行测试
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private RouteService routeService;
    @Autowired
    private RouterService routerService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody Map<String, Object> body) {

        Long routeId = body.get("routeId") != null
                ? ((Number) body.get("routeId")).longValue() : null;
        if (routeId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "routeId is required"));
        }

        Route route = routeService.findById(routeId).orElse(null);
        if (route == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Route not found"));
        }
        if (route.getPrimaryModelId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Route has no primary model configured. Please select a model in route settings."));
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
        if (messages == null || messages.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "messages is required"));
        }

        Double temperature = body.get("temperature") != null
                ? ((Number) body.get("temperature")).doubleValue() : null;
        Integer maxTokens = body.get("max_tokens") != null
                ? ((Number) body.get("max_tokens")).intValue() : 256;

        try {
            Map<String, Object> response = routerService.routeByRoute(route, messages, temperature, maxTokens);
            return ResponseEntity.ok(response);
        } catch (RouterService.RouterException e) {
            Map<String, Object> errBody = new java.util.HashMap<>();
            errBody.put("error", e.getMessage());
            errBody.put("router_log", e.getAttempts());
            return ResponseEntity.status(502).body(errBody);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Model request failed";
            return ResponseEntity.status(502).body(Map.of("error", msg));
        }
    }
}
