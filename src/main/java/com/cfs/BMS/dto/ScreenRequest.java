package com.cfs.BMS.dto;

import jakarta.validation.constraints.*;

/**
 * Creates a screen together with its seat layout. Rows are lettered A.. from the front;
 * the last {@code vipRows} rows are VIP and the {@code premiumRows} rows before them are PREMIUM.
 */
public record ScreenRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long theaterId,
        @NotNull @Min(1) @Max(26) Integer rows,
        @NotNull @Min(1) @Max(50) Integer seatsPerRow,
        @Min(0) Integer premiumRows,
        @Min(0) Integer vipRows) {
}
