package com.cfs.BMS.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** The user is taken from the authenticated token, never from the body. */
public record BookingRequest(
        @NotNull Long showId,
        @NotEmpty List<@NotNull Long> seatIds) {
}
