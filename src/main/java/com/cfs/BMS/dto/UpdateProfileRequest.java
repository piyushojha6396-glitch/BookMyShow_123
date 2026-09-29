package com.cfs.BMS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String name,
        @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "must be a valid phone number") String phone) {
}
