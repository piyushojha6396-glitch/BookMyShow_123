package com.cfs.BMS.dto;

import com.cfs.BMS.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(Long id, String name, String email, String phone, Role role,
                           boolean active, LocalDateTime createdAt) {
}
