package com.cfs.BMS.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds, UserResponse user) {
}
