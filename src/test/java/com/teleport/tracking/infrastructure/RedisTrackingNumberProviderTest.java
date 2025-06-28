package com.teleport.tracking.infrastructure;

import com.teleport.tracking.domain.TrackingNumberGenerationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;

class RedisTrackingNumberProviderTest {

    @Mock
    private RedisService redisService;

    private RedisTrackingNumberProvider redisTrackingNumberProvider;
    private final Long batchSize = 100L;

    @BeforeEach
    void setUp() {
        redisService = Mockito.mock(RedisService.class);
        redisTrackingNumberProvider = new RedisTrackingNumberProvider(redisService, batchSize);
    }

    @Test
    void testInitialization() {
        Mockito.when(redisService.incrementCounterWithDelta(eq(RedisConstants.TRACKING_COUNTER_KEY), anyLong()))
                .thenReturn(Mono.just(batchSize)); // Simulate Redis increment response
        StepVerifier.create(redisTrackingNumberProvider.nextCounter())
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void testNextCounterWithinRange() {
        Mockito.when(redisService.incrementCounterWithDelta(eq(RedisConstants.TRACKING_COUNTER_KEY), anyLong()))
                .thenReturn(Mono.just(batchSize)); // Simulate Redis increment response
        StepVerifier.create(redisTrackingNumberProvider.nextCounter())
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(redisTrackingNumberProvider.nextCounter())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void testRangeRefresh() {
        Mockito.when(redisService.incrementCounterWithDelta(eq(RedisConstants.TRACKING_COUNTER_KEY), anyLong()))
                .thenReturn(Mono.just(2 * batchSize));

        StepVerifier.create(redisTrackingNumberProvider.nextCounter())
                .expectNext(batchSize + 1L)
                .verifyComplete();
    }

    @Test
    void testConcurrentNextCounterWithoutRangeRefresh() throws InterruptedException, ExecutionException {
        setupAndAssert(10, 10, 1);

    }
    @Test
    void testConcurrentNextCounterRangeRefreshWhenRangeExceeds() throws InterruptedException, ExecutionException {
        setupAndAssert(2, 100, 2);
    }
    @Test
    void testNextCounterWithAllThreadStartingAtAlmostSameTime() throws InterruptedException, ExecutionException {
        setupAndAssert(100, 1000, 1000);
    }
    private void setupAndAssert(int threadCount, int callsPerThread, int expectedRedisCallCount) throws InterruptedException, ExecutionException {
        int totalCalls = threadCount * callsPerThread;
        AtomicInteger redisCallCount = new AtomicInteger();

        Mockito.when(redisService.incrementCounterWithDelta(eq(RedisConstants.TRACKING_COUNTER_KEY), anyLong()))
                .thenAnswer(invocation -> {
                    int call = redisCallCount.incrementAndGet();
                    return Mono.just(batchSize * call);
                });

        Flux<Long> all = Flux.range(0, totalCalls)
                .flatMap(i -> redisTrackingNumberProvider.nextCounter(), threadCount);

        StepVerifier.create(all.collectList())
                .assertNext(list -> {
                    Set<Long> unique = new HashSet<>(list);
                    Assertions.assertEquals(totalCalls, unique.size());
                    Assertions.assertEquals(expectedRedisCallCount, redisCallCount.get());
                })
                .verifyComplete();
    }
    @Test
    void testRedisServiceException_propagatesTrackingNumberGenerationException() {
        Mockito.when(redisService.incrementCounterWithDelta(eq(RedisConstants.TRACKING_COUNTER_KEY), anyLong()))
                .thenReturn(Mono.error(new TrackingNumberGenerationException("Redis failure", null)));
        RedisTrackingNumberProvider provider = new RedisTrackingNumberProvider(redisService);

        StepVerifier.create(provider.nextCounter())
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertTrue(throwable instanceof TrackingNumberGenerationException);
                    Assertions.assertTrue(throwable.getMessage().contains("Redis failure"));
                })
                .verify();
    }

}