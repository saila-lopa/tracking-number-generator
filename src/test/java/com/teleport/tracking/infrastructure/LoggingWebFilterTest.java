package com.teleport.tracking.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingWebFilterTest {
    private LoggingWebFilter filter;
    private ServerWebExchange exchange;
    private WebFilterChain chain;
    private ServerHttpRequest request;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        filter = new LoggingWebFilter();
        exchange = mock(ServerWebExchange.class);
        chain = mock(WebFilterChain.class);
        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getURI()).thenReturn(URI.create("/test"));
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
    }

    @Test
    void filter_traceIdPresent_usesHeaderValue() {
        HttpHeaders headers = new HttpHeaders();
        String traceId = UUID.randomUUID().toString();
        headers.add(LoggingWebFilter.TRACE_ID_HEADER, traceId);
        when(request.getHeaders()).thenReturn(headers);
        // Use a custom chain to assert MDC inside the reactive context
        WebFilterChain customChain = exchange -> Mono.fromRunnable(() -> {
            assertEquals(traceId, MDC.get(LoggingWebFilter.TRACE_ID_KEY));
        });

        StepVerifier.create(filter.filter(exchange, customChain)).verifyComplete();
        // No assertion on MDC here, as it is cleaned up after the chain
    }

    @Test
    void filter_traceIdAbsent_generatesTraceId() {
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        WebFilterChain customChain = exchange -> Mono.fromRunnable(() -> {
            String mdcTraceId = MDC.get(LoggingWebFilter.TRACE_ID_KEY);
            assertNotNull(mdcTraceId);
            assertFalse(mdcTraceId.isEmpty());
        });

        StepVerifier.create(filter.filter(exchange, customChain)).verifyComplete();
    }

    @Test
    void filter_removesMdcOnSuccess() {
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        assertNull(MDC.get(LoggingWebFilter.TRACE_ID_KEY));
    }

    @Test
    void filter_removesMdcOnError() {
        HttpHeaders headers = new HttpHeaders();
        when(request.getHeaders()).thenReturn(headers);
        when(chain.filter(exchange)).thenReturn(Mono.error(new RuntimeException("fail")));

        StepVerifier.create(filter.filter(exchange, chain)).expectError().verify();
        assertNull(MDC.get(LoggingWebFilter.TRACE_ID_KEY));
    }
}
