package com.teleport.tracking.app;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public interface TrackingService {
    Mono<String> generateTrackingNumber();
}
