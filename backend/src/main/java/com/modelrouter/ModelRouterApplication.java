/**
 * Spring Boot application entry point. Starts the embedded web server and loads configuration.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ModelRouterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelRouterApplication.class, args);
    }
}
