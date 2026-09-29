package com.cfs.BMS.dto;

import com.cfs.BMS.enums.SeatType;
import jakarta.validation.constraints.*;

public record SeatRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z]{1,2}$", message = "must be 1-2 letters") String row,
        @NotNull @Min(1) @Max(99) Integer col,
        @NotNull SeatType seatType,
        @NotNull Long screenId) {
}
