package com.modelrouter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modelrouter.service.ApiKeyService;
import com.modelrouter.service.RouteService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 拦截 /v1/* 请求，验证 Authorization 必须存在且与 router 中的 key 匹配
 */
@Component
@Order(1)
public class V1AuthFilter implements Filter {

    private final ApiKeyService apiKeyService;
    private final RouteService routeService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public V1AuthFilter(ApiKeyService apiKeyService, RouteService routeService) {
        this.apiKeyService = apiKeyService;
        this.routeService = routeService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        if (!uri.startsWith("/v1") && !uri.startsWith("/api/v1")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String authorization = req.getHeader("Authorization");
            if (authorization == null || authorization.isBlank()) {
                writeError(resp, 401, "1001", "Header中未收到Authorization参数，无法进行身份验证。");
                return;
            }

            String token = authorization.startsWith("Bearer ") ? authorization.substring(7).trim() : authorization.trim();
            if (token.isEmpty()) {
                writeError(resp, 401, "1001", "Header中未收到Authorization参数，无法进行身份验证。");
                return;
            }

            if (!isValidToken(token)) {
                writeError(resp, 401, "invalid_api_key", "Invalid API key");
                return;
            }

            chain.doFilter(request, response);
        } catch (Exception e) {
            if (!resp.isCommitted()) {
                resp.setStatus(500);
                resp.setContentType("application/json; charset=UTF-8");
                Map<String, Object> body = Map.of("error", Map.of("code", "internal_error", "message", e.getMessage() != null ? e.getMessage() : "Server error"));
                resp.getWriter().write(objectMapper.writeValueAsString(body));
            }
            throw new ServletException(e);
        }
    }

    private boolean isValidToken(String token) {
        if (apiKeyService.findByKey(token).isPresent()) return true;
        if (apiKeyService.findBySecret(token).isPresent()) return true;
        if (routeService.findByApiKey(token).isPresent()) return true;
        return false;
    }

    private void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        Map<String, Object> body = Map.of("error", Map.of("code", code, "message", message));
        resp.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
