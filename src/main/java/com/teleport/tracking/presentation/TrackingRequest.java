package com.teleport.tracking.presentation;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@AllArgsConstructor
public class TrackingRequest {
    @Pattern(regexp = "^[A-Z]{2}$", message = "origin_country_id must be ISO 3166-1 uppercase alpha-2")
    public String originCountryId;

    @Pattern(regexp = "^[A-Z]{2}$", message = "destination_country_id must be ISO 3166-1 uppercase alpha-2")
    public String destinationCountryId;

    @DecimalMin(value = "0.001", inclusive = true, message = "weight too low, must be at least 0.001")
    @Digits(integer = 10, fraction = 3, message = "weight can be up to 3 decimal places")
    public BigDecimal weight;

    @NotNull(message = "created_at is required")
    public OffsetDateTime createdAt;

    @NotBlank(message = "customer_id is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}$",
            message = "customer_id must be a valid UUID")
    public String customerId;

    @NotBlank(message = "customer_name is required")
    public String customerName;

    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "customer_slug must be kebab-case")
    public String customerSlug;

    public static TrackingRequest fromRequest(
            String originCountryId,
            String destinationCountryId,
            String weightStr,
            String createdAtStr,
            String customerId,
            String customerName,
            String customerSlug
    ) {
        BigDecimal weight;
        try {
            weight = new BigDecimal(weightStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException( "Invalid weight");
        }
        OffsetDateTime createdAt;
        try {
            if (createdAtStr != null) {
                if(createdAtStr.contains(" "))
                    createdAtStr = createdAtStr.replace(" ", "+");
                createdAt = OffsetDateTime.parse(createdAtStr);
            } else createdAt = null;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid created_at");
        }
        return new TrackingRequest(
                originCountryId,
                destinationCountryId,
                weight,
                createdAt,
                customerId,
                customerName,
                customerSlug
        );
    }
}
