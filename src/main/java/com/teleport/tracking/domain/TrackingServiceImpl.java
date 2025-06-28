package com.teleport.tracking.domain;

import com.teleport.tracking.app.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sqids.Sqids;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
public class TrackingServiceImpl implements TrackingService {
    public static final String ALPHABETSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int TRACKING_NUMBER_LENGTH = 16;
    private final TrackingNumberProvider trackingNumberProvider;

    private final Sqids sqids = Sqids.builder()
            .alphabet(ALPHABETSET)
            .minLength(TRACKING_NUMBER_LENGTH)
            .build();

    @Autowired
    public TrackingServiceImpl(TrackingNumberProvider trackingNumberProvider) {
        this.trackingNumberProvider = trackingNumberProvider;
    }

    @Override
    public Mono<String> generateTrackingNumber() {
        return trackingNumberProvider.nextCounter()
            .map(count -> sqids.encode(Collections.singletonList(count)))
            .onErrorMap(ex -> new TrackingNumberGenerationException("Failed to generate tracking number", ex));
    }
}