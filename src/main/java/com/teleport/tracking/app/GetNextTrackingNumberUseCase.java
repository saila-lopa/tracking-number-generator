package com.teleport.tracking.app;

import com.teleport.tracking.presentation.TrackingRequest;
import com.teleport.tracking.presentation.TrackingResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Component
@Slf4j
public class GetNextTrackingNumberUseCase {

    TrackingService trackingService;
    private final Validator validator;


    @Autowired
    public GetNextTrackingNumberUseCase(TrackingService trackingService, Validator validator) {
        this.trackingService = trackingService;
        this.validator = validator;
    }

    public Mono<TrackingResponse> generateTrackingNumber(TrackingRequest trackingRequest) {
        Set<ConstraintViolation<TrackingRequest>> violations = validator.validate(trackingRequest);
        if (!violations.isEmpty()) {
            String errorMsg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((m1, m2) -> m1 + ". " + m2)
                    .orElse("Validation failed");
            throw new ServerWebInputException(errorMsg);
        }
        return trackingService.generateTrackingNumber()
                .map(id -> {
                    log.info("Generated tracking number: {}", id);
                    return new TrackingResponse(id, OffsetDateTime.now(java.time.ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                });
    }
}
