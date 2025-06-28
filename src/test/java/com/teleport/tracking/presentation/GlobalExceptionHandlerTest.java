package com.teleport.tracking.presentation;

import com.teleport.tracking.domain.TrackingNumberGenerationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebInputException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleTrackingNumberGenerationException_returnsInternalServerError() {
        TrackingNumberGenerationException ex = new TrackingNumberGenerationException("Generation failed", null);
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleTrackingNumberGenerationException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TRACKING_NUMBER_ERROR", response.getBody().getCode());
        assertEquals("Generation failed", response.getBody().getMessage());
    }

    @Test
    void handleWebInputException_returnsBadRequest() {
        ServerWebInputException ex = new ServerWebInputException("Invalid input");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleWebInputException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("BAD_REQUEST", response.getBody().getCode());
        assertEquals("Invalid input", response.getBody().getMessage());
    }

    @Test
    void handleGenericException_returnsInternalServerError() {
        Exception ex = new Exception("Some error");
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
