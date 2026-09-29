package com.cfs.BMS.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** endTime is optional - when omitted it is derived from the movie duration. */
public record ShowRequest(
        @NotNull Long movieId,
        @NotNull Long screenId,
        @NotNull LocalDate showDate,
        @NotNull LocalTime startTime,
        LocalTime endTime,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @Digits(integer = 8, fraction = 2)
        BigDecimal ticketPrice) {
}
