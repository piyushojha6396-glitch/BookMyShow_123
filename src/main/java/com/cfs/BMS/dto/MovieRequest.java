package com.cfs.BMS.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MovieRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        @Size(max = 50) String genre,
        @Size(max = 50) String language,
        @NotNull @Min(1) @Max(600) Integer durationMinutes,
        @DecimalMin("0.0") @DecimalMax("10.0") Double rating,
        LocalDate releaseDate,
        @Size(max = 500) String posterUrl) {
}
