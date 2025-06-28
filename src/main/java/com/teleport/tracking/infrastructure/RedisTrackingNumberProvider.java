package com.teleport.tracking.infrastructure;

import com.teleport.tracking.domain.TrackingNumberGenerationException;
import com.teleport.tracking.domain.TrackingNumberProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.teleport.tracking.infrastructure.RedisConstants.TRACKING_COUNTER_KEY;

@Service
@Slf4j
public class RedisTrackingNumberProvider implements TrackingNumberProvider {

    private final AtomicLong nextValue = new AtomicLong(0);
    private final AtomicLong rangeEnd = new AtomicLong(0);
    private final RedisService redisService;
    private final AtomicBoolean refreshing = new AtomicBoolean(false);
    private final Long batchSize;

    public RedisTrackingNumberProvider(RedisService redisService, Long batchSize) {
        this.redisService = redisService;
        this.batchSize = batchSize;
    }

    @Autowired
    public RedisTrackingNumberProvider(RedisService redisService) {
        this(redisService, RedisConstants.BATCH_SIZE);
    }

    @Override
    public Mono<Long> nextCounter() {
        return Mono.defer(() -> {
            long current = nextValue.getAndIncrement();
            if (current < rangeEnd.get()) {
                return Mono.just(current);
            }
            if (refreshing.compareAndSet(false, true)) {
                return refreshRange()
                        .doOnError(ex -> {
                            refreshing.set(false);
                            throw (TrackingNumberGenerationException) ex;
                        })
                        .doFinally(signal -> refreshing.set(false))
                        .then(Mono.fromSupplier(() -> nextValue.getAndIncrement()));
            } else {
                return Mono.defer(() -> {
                    if (nextValue.get() < rangeEnd.get()) {
                        return Mono.just(nextValue.getAndIncrement());
                    }
                    return Mono.delay(java.time.Duration.ofMillis(5))
                            .flatMap(tick -> nextCounter());
                });
            }
        });
    }

    private Mono<Void> refreshRange() {
        return redisService.incrementCounterWithDelta(TRACKING_COUNTER_KEY, batchSize)
                .doOnNext(newEnd -> {
                    long start = newEnd - batchSize + 1;
                    nextValue.set(start);
                    rangeEnd.set(newEnd + 1);
                    log.info("set nextValue to {} and rangeEnd to {}", start, newEnd + 1);
                })
                .then();
    }

}