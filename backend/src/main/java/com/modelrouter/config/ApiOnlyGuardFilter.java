/**
 * Restricts certain paths to API-only mode when configured.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * When serve-spa=false (API-only mode), reject all non-API requests with 401.
 * Prevents 404 stack traces for GET /, /index.html, /favicon.ico etc.
 */
@Component
@Order(-1)
@ConditionalOnProperty(name = "modelrouter.serve-spa", havingValue = "false")
public class ApiOnlyGuardFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        if (uri.startsWith("/api") || uri.startsWith("/v1")) {
            chain.doFilter(request, response);
            return;
        }

        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(
                Map.of("error", Map.of("code", "unauthorized", "message", "Access Denied"))));
    }
}
