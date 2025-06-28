package com.teleport.tracking.domain;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface TrackingNumberProvider {
    Mono<Long> nextCounter();
}
