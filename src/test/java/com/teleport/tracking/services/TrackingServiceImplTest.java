package com.teleport.tracking.services;

import com.teleport.tracking.domain.TrackingNumberGenerationException;
import com.teleport.tracking.domain.TrackingNumberProvider;
import com.teleport.tracking.domain.TrackingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TrackingServiceImplTest {
    @Mock
    private TrackingNumberProvider trackingNumberProvider;
    private TrackingServiceImpl trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new TrackingServiceImpl(trackingNumberProvider);
    }

    @Test
    void generateTrackingNumber_shouldReturnEncodedTrackingNumber() {
        long counterValue = 12345L;
        Mockito.when(trackingNumberProvider.nextCounter()).thenReturn(Mono.just(counterValue));

        Mono<String> result = trackingService.generateTrackingNumber();

        StepVerifier.create(result)
                .expectNextMatches(trackingNumber -> trackingNumber != null && trackingNumber.length() >= 12)
                .verifyComplete();
    }

    @Test
    void generateTrackingNumber_shouldPropagateError() {
        RuntimeException error = new RuntimeException("Counter error");
        Mockito.when(trackingNumberProvider.nextCounter()).thenReturn(Mono.error(error));

        Mono<String> result = trackingService.generateTrackingNumber();

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof TrackingNumberGenerationException && throwable.getCause().getMessage().equals("Counter error"))
                .verify();
    }

    @Test
    void generateTrackingNumber_shouldOnlyContainAllowedCharactersAndHaveLength16() {
        long counterValue = 987654321L;
        Mockito.when(trackingNumberProvider.nextCounter()).thenReturn(Mono.just(counterValue));

        Mono<String> result = trackingService.generateTrackingNumber();

        StepVerifier.create(result)
                .assertNext(trackingNumber -> {
                    // Check only A-Z0-9
                    boolean matches = trackingNumber.matches("^[A-Z0-9]+$");
                    // Check length is 16
                    boolean lengthOk = trackingNumber.length() == 16;
                    assert matches : "Tracking number contains invalid characters: " + trackingNumber;
                    assert lengthOk : "Tracking number length is not 16: " + trackingNumber.length();
                })
                .verifyComplete();
    }
}
