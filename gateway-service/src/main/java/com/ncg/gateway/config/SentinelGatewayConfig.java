package com.ncg.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Sentinel Gateway 限流配置
 */
@Component
public class SentinelGatewayConfig extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            return chain.filter(exchange).onErrorResume(e -> {
                String className = e.getClass().getSimpleName();
                if (className.contains("BlockException") || 
                    className.contains("SentinelException") ||
                    (e.getMessage() != null && e.getMessage().contains("Sentinel"))) {
                    return limitBlocked(exchange.getResponse());
                }
                return Mono.error(e);
            });
        };
    }

    private Mono<Void> limitBlocked(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
