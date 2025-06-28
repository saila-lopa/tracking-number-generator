package com.teleport.tracking.infrastructure;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RedisServiceTest {
    @Test
    void incrementCounterWithDelta_returnsIncrementedValue() {
        ReactiveValueOperations<String, String> valueOps = Mockito.mock(ReactiveValueOperations.class);
        when(valueOps.increment(eq("counter"), eq(5L))).thenReturn(Mono.just(42L));
        RedisService redisService = new RedisService(valueOps);

        StepVerifier.create(redisService.incrementCounterWithDelta("counter", 5L))
                .expectNext(42L)
                .verifyComplete();
    }

    @Test
    void incrementCounterWithDelta_propagatesError() {
        ReactiveValueOperations<String, String> valueOps = Mockito.mock(ReactiveValueOperations.class);
        when(valueOps.increment(eq("counter"), eq(1L))).thenReturn(Mono.error(new RuntimeException("Redis error")));
        RedisService redisService = new RedisService(valueOps);

        StepVerifier.create(redisService.incrementCounterWithDelta("counter", 1L))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Redis error"))
                .verify();
    }
}

