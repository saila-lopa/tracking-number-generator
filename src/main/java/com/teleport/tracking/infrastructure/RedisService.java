// RedisService.java
package com.teleport.tracking.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RedisService {

    @Autowired
    public RedisService(ReactiveRedisConnectionFactory factory) {
        ReactiveStringRedisTemplate template = new ReactiveStringRedisTemplate(factory);
        this.valueOps = template.opsForValue();
    }

    private final ReactiveValueOperations<String, String> valueOps;

    public RedisService(ReactiveValueOperations<String, String> valueOps) {
        this.valueOps = valueOps;
    }

    public Mono<Long> incrementCounterWithDelta(String key, long delta) {
        return valueOps.increment(key, delta);
    }
}