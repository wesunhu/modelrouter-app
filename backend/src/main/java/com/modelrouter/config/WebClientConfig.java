/**
 * Configures WebClient for outbound HTTP to upstream LLM APIs.
 *
 * @version 1.0.1
 * @since 2026-03-21
 * @author wesun hu
 */

package com.modelrouter.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置：非阻塞 HTTP 客户端，支持连接池与全局超时
 * 每次请求可通过 timeout 覆盖响应超时
 */
@Configuration
public class WebClientConfig {

    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 120_000;
    private static final int DEFAULT_WRITE_TIMEOUT_MS = 60_000;
    private static final int MAX_CONNECTIONS = 100;

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT_MS)
                .responseTimeout(Duration.ofMillis(DEFAULT_READ_TIMEOUT_MS))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(DEFAULT_READ_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(DEFAULT_WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
