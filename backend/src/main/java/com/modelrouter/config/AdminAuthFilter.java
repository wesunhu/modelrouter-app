/**
 * Servlet filter enforcing admin session for protected /api routes.
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
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 保护管理端 API：/api/* 除 /api/auth/* 外均需登录
 */
@Component
@Order(1)
public class AdminAuthFilter implements Filter {

    private static final String SESSION_ATTR_USER = "admin_username";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        if (!uri.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        if (uri.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        String username = session != null ? (String) session.getAttribute(SESSION_ATTR_USER) : null;
        if (username == null || username.isBlank()) {
            writeError(resp, 401, "unauthorized", "Please log in first");
            return;
        }

        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse resp, int status, String code, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        Map<String, Object> body = Map.of("error", Map.of("code", code, "message", message));
        resp.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
