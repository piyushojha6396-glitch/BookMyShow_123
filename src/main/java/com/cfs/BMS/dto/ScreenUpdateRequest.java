package com.cfs.BMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScreenUpdateRequest(@NotBlank @Size(max = 100) String name) {
}
