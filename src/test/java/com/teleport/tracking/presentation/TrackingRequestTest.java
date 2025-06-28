package com.teleport.tracking.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TrackingRequestTest {
    @Test
    void fromRequest_validInput_createsTrackingRequest() {
        String originCountryId = "US";
        String destinationCountryId = "CA";
        String weightStr = "1.23";
        String createdAtStr = OffsetDateTime.now().toString();
        String customerId = "123e4567-e89b-12d3-a456-426614174000";
        String customerName = "John Doe";
        String customerSlug = "john-doe";

        TrackingRequest req = assertDoesNotThrow(() ->
            TrackingRequest.fromRequest(originCountryId, destinationCountryId, weightStr, createdAtStr, customerId, customerName, customerSlug)
        );
        assertEquals(new BigDecimal(weightStr), req.weight);
        assertEquals(originCountryId, req.originCountryId);
        assertEquals(destinationCountryId, req.destinationCountryId);
        assertEquals(customerId, req.customerId);
        assertEquals(customerName, req.customerName);
        assertEquals(customerSlug, req.customerSlug);
        assertNotNull(req.createdAt);
    }

    @Test
    void fromRequest_invalidWeight_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            TrackingRequest.fromRequest("US", "CA", "not-a-number", OffsetDateTime.now().toString(), "123e4567-e89b-12d3-a456-426614174000", "John Doe", "john-doe")
        );
        assertTrue(ex.getMessage().contains("Invalid weight"));
    }

    @Test
    void fromRequest_invalidCreatedAt_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            TrackingRequest.fromRequest("US", "CA", "1.23", "not-a-date", "123e4567-e89b-12d3-a456-426614174000", "John Doe", "john-doe")
        );
        assertTrue(ex.getMessage().contains("Invalid created_at"));
    }

    @Test
    void fromRequest_emptyCreatedAt_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            TrackingRequest.fromRequest("US", "CA", "1.23", "", "123e4567-e89b-12d3-a456-426614174000", "John Doe", "john-doe")
        );
        assertTrue(ex.getMessage().contains("Invalid created_at"));
    }

    @Test
    void fromRequest_createdAtWithSpace_parsesSuccessfully() {
        String now = OffsetDateTime.now().toString().replace("+", " ");
        TrackingRequest req = assertDoesNotThrow(() ->
            TrackingRequest.fromRequest("US", "CA", "1.23", now, "123e4567-e89b-12d3-a456-426614174000", "John Doe", "john-doe")
        );
        assertNotNull(req.createdAt);
    }

    @Test
    void fromRequest_invalidCountryCodes_createsRequestButFailsValidation() {
        TrackingRequest req = TrackingRequest.fromRequest(
                "U1", // invalid
                "C@", // invalid
                "1.23",
                OffsetDateTime.now().toString(),
                "123e4567-e89b-12d3-a456-426614174000",
                "John Doe",
                "john-doe"
        );
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("origin_country_id must be ISO 3166-1 uppercase alpha-2")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("destination_country_id must be ISO 3166-1 uppercase alpha-2")));
    }

    @Test
    void fromRequest_blankCustomerIdOrNameOrInvalidSlug_failsValidation() {
        TrackingRequest req = TrackingRequest.fromRequest(
                "US",
                "CA",
                "1.23",
                OffsetDateTime.now().toString(),
                "", // blank customerId
                "", // blank customerName
                "Not-Kebab_Case" // invalid slug
        );
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("customer_id is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("customer_name is required")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("customer_slug must be kebab-case")));
    }

    @Test
    void fromRequest_invalidCustomerIdPattern_failsValidation() {
        TrackingRequest req = TrackingRequest.fromRequest(
                "US",
                "CA",
                "1.23",
                OffsetDateTime.now().toString(),
                "not-a-uuid",
                "John Doe",
                "john-doe"
        );
        var validator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
        var violations = validator.validate(req);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("customer_id must be a valid UUID")));
    }
}
