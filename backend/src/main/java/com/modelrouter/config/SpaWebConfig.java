package com.modelrouter.config;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SQLite 单机模式下：后端托管前端静态资源，支持 SPA 路由回退
 */
@Component
@Profile("sqlite")
public class SpaWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String path, Resource location) throws IOException {
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
