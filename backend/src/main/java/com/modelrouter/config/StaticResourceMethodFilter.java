package com.modelrouter.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 拦截对静态资源路径的 POST/PUT/DELETE/PATCH 请求，避免 ResourceHttpRequestHandler 抛出 405。
 * 仅 GET/HEAD 应访问 SPA 静态资源；非 API 路径的其他方法返回 405。
 */
@Component
@Order(0)
public class StaticResourceMethodFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String method = req.getMethod();
        String uri = req.getRequestURI();

        // 仅对非 GET/HEAD 且非 API 路径的请求进行拦截
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            if (!uri.startsWith("/api") && !uri.startsWith("/v1")) {
                resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"error\":{\"code\":\"method_not_allowed\",\"message\":\"POST is not supported for this path. Use /api/* or /v1/* for API requests.\"}}");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
