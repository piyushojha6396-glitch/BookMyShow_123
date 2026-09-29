package com.cfs.BMS;

import com.cfs.BMS.config.AppProperties;
import com.cfs.BMS.entity.User;
import com.cfs.BMS.enums.Role;
import com.cfs.BMS.security.JwtService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static AppProperties props(String secret, long minutes) {
        return new AppProperties(
                new AppProperties.Jwt(secret, minutes, "BMS"),
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Booking(10, 2),
                new AppProperties.Admin("Admin", null, null),
                "Asia/Kolkata");
    }

    private static User user() {
        return User.builder().id(42L).name("Test").email("t@example.com").role(Role.USER).build();
    }

    @Test
    void validTokenReturnsUserId() {
        JwtService service = new JwtService(props("0123456789abcdef0123456789abcdef", 5));
        String token = service.generateToken(user());
        assertEquals(42L, service.extractUserId(token).orElseThrow());
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(props("0123456789abcdef0123456789abcdef", 5));
        String token = service.generateToken(user());
        assertTrue(service.extractUserId(token + "x").isEmpty());
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService a = new JwtService(props("0123456789abcdef0123456789abcdef", 5));
        JwtService b = new JwtService(props("ffffffffffffffffffffffffffffffff", 5));
        assertTrue(b.extractUserId(a.generateToken(user())).isEmpty());
    }

    @Test
    void garbageIsRejected() {
        JwtService service = new JwtService(props("0123456789abcdef0123456789abcdef", 5));
        assertTrue(service.extractUserId("not-a-jwt").isEmpty());
    }
}
