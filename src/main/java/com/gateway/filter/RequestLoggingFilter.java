package com.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();
        String client = String.valueOf(exchange.getRequest().getRemoteAddress());
        long startTime = System.nanoTime();

        return chain.filter(exchange)
                .doFinally(signal -> {
                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                    int status = exchange.getResponse().getStatusCode() == null
                            ? 0
                            : exchange.getResponse().getStatusCode().value();

                    log.info("method={} path={} client={} status={} durationMs={}",
                            method, path, client, status, durationMs);
                });
    }

    @Override
    public int getOrder() {
        return -200;
    }
}
