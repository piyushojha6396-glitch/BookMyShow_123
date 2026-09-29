package com.cfs.BMS.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Strongly typed application settings (prefix "app"). Validated on startup so that
 * a missing JWT secret makes the application fail fast instead of running insecurely.
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @DefaultValue Cors cors,
        @Valid @DefaultValue Booking booking,
        @DefaultValue Admin admin,
        @DefaultValue("Asia/Kolkata") String timezone) {

    public record Jwt(
            @NotBlank @Size(min = 32, message = "app.jwt.secret must be at least 32 characters") String secret,
            @DefaultValue("60") @Min(1) long expirationMinutes,
            @DefaultValue("BMS") String issuer) {
    }

    public record Cors(@DefaultValue("http://localhost:3000") List<String> allowedOrigins) {
    }

    public record Booking(
            @DefaultValue("10") @Min(1) int maxSeatsPerBooking,
            @DefaultValue("2") @Min(0) int cancellationWindowHours) {
    }

    /** Optional bootstrap admin account, created on startup when email and password are both set. */
    public record Admin(
            @DefaultValue("Administrator") String name,
            String email,
            String password) {
    }
}
