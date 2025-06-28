package com.teleport.tracking.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
public class LoggingWebFilter implements WebFilter {
    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().toString();
        String traceId = resolveOrGenerateTraceId(exchange);
        MDC.put(TRACE_ID_KEY, traceId);
        logRequest(method, path);

        ServerHttpResponse originalResponse = exchange.getResponse();

        return chain.filter(exchange)
            .doOnSuccess(aVoid -> {
                int status = originalResponse.getStatusCode() != null ? originalResponse.getStatusCode().value() : 0;
                logResponse(method, path, status);
                MDC.remove(TRACE_ID_KEY);
            })
            .doOnError(e -> MDC.remove(TRACE_ID_KEY));
    }
    private String resolveOrGenerateTraceId(ServerWebExchange exchange) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        return (traceId == null || traceId.isEmpty()) ? UUID.randomUUID().toString() : traceId;
    }

    private void logRequest(String method, String path) {
        log.info("Incoming request: {} {}", method, path);
    }

    private void logResponse(String method, String path, int status) {
        log.info("Outgoing response: {} {} - Path: {}", method,status, path);
    }
}
