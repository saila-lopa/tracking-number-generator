package com.teleport.tracking.app;

import com.teleport.tracking.presentation.TrackingRequest;
import com.teleport.tracking.presentation.TrackingResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetNextTrackingNumberUseCaseTest {
    @Mock
    private TrackingService trackingService;
    @Mock
    private Validator validator;

    private GetNextTrackingNumberUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetNextTrackingNumberUseCase(trackingService, validator);
    }

    @Test
    void generateTrackingNumber_validRequest_returnsTrackingResponse() {
        TrackingRequest request = mock(TrackingRequest.class);
        when(validator.validate(request)).thenReturn(Collections.emptySet());
        when(trackingService.generateTrackingNumber()).thenReturn(Mono.just("TRACK123"));

        Mono<TrackingResponse> result = useCase.generateTrackingNumber(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("TRACK123", response.getTracking_number());
                    assertDoesNotThrow(() -> OffsetDateTime.parse(response.getCreated_at()));
                })
                .verifyComplete();
    }

    @Test
    void generateTrackingNumber_invalidRequest_throwsException() {
        TrackingRequest request = mock(TrackingRequest.class);
        ConstraintViolation<TrackingRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Invalid field");
        Set<ConstraintViolation<TrackingRequest>> violations = Set.of(violation);
        when(validator.validate(request)).thenReturn(violations);

        assertThrows(ServerWebInputException.class, () -> useCase.generateTrackingNumber(request));
    }

    @Test
    void generateTrackingNumber_validRequest_realValidation_returnsTrackingResponse() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        TrackingRequest request = new TrackingRequest(
                "US", // valid ISO country code
                "CA", // valid ISO country code
                new BigDecimal("1.234"), // valid weight
                OffsetDateTime.now(), // valid date
                "123e4567-e89b-12d3-a456-426614174000", // valid UUID
                "John Doe", // valid name
                "john-doe" // valid slug
        );
        when(trackingService.generateTrackingNumber()).thenReturn(Mono.just("TRACK123"));

        Mono<TrackingResponse> result = useCase.generateTrackingNumber(request);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals("TRACK123", response.getTracking_number());
                    assertDoesNotThrow(() -> OffsetDateTime.parse(response.getCreated_at()));
                })
                .verifyComplete();
    }

    @Test
    void generateTrackingNumber_invalidRequest_realValidation_throwsException() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        TrackingRequest request = new TrackingRequest(
                "USA",
                "C",
                new BigDecimal("0.0001"),
                null,
                "not-a-uuid",
                "",
                "Not Kebab Case"
        );
        useCase = new GetNextTrackingNumberUseCase(trackingService, validator);
        assertThrows(ServerWebInputException.class, () -> useCase.generateTrackingNumber(request));
    }

    @Test
    void trackingRequest_invalidFields_reportsExpectedValidationMessages() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        TrackingRequest request = new TrackingRequest(
                "USA",
                "C",
                new BigDecimal("0.0001"),
                null,
                "not-a-uuid",
                "",
                "Not Kebab Case"
        );
        Set<ConstraintViolation<TrackingRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        var messages = violations.stream().map(ConstraintViolation::getMessage).toList();
        assertTrue(messages.contains("origin_country_id must be ISO 3166-1 uppercase alpha-2"));
        assertTrue(messages.contains("destination_country_id must be ISO 3166-1 uppercase alpha-2"));
        assertTrue(messages.contains("weight too low, must be at least 0.001"));
        assertTrue(messages.contains("created_at is required"));
        assertTrue(messages.contains("customer_id must be a valid UUID"));
        assertTrue(messages.contains("customer_name is required"));
        assertTrue(messages.contains("customer_slug must be kebab-case"));
    }
}
