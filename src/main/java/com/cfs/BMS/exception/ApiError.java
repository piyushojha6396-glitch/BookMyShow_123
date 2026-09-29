package com.cfs.BMS.exception;

import java.time.Instant;
import java.util.List;

/** Uniform error body returned for every failure. */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> errors) {

    public record FieldViolation(String field, String message) {
    }
}
