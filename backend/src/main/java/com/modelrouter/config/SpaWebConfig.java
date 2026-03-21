/**
 * Serves SPA static resources when enabled.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SQLite 单机模式下：后端托管前端静态资源（可选）
 * modelrouter.serve-spa=true 时启用；launcher 启动时传 false，实现前后端完全分离
 */
@Component
@Profile("sqlite")
@ConditionalOnProperty(name = "modelrouter.serve-spa", havingValue = "true", matchIfMissing = true)
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String path, Resource location) throws IOException {
                        // 勿将 /api/* 回退到 index.html，否则 GET /api/... 会误返回 SPA，前端误把 HTML 当 JSON
                        if (path != null && (path.startsWith("api/") || path.startsWith("/api/"))) {
                            return null;
                        }
                        Resource resource = location.createRelative(path);
                        if (resource.exists() && resource.isReadable()) {
                            return resource;
                        }
                        Resource index = location.createRelative("index.html");
                        return index.exists() ? index : resource;
                    }
                });
    }
}
