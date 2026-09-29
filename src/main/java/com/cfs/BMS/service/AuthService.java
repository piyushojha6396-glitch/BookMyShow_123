package com.cfs.BMS.service;

import com.cfs.BMS.dto.AuthResponse;
import com.cfs.BMS.dto.LoginRequest;
import com.cfs.BMS.dto.UserRequest;
import com.cfs.BMS.entity.User;
import com.cfs.BMS.enums.Role;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.UnauthorizedException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.UserRepository;
import com.cfs.BMS.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BAD_CREDENTIALS = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;

    /** Public registration always creates a normal USER; admins are created via configuration. */
    @Transactional
    public AuthResponse register(UserRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        User user = userRepository.save(User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .role(Role.USER)
                .active(true)
                .build());
        log.info("New user registered: id={}", user.getId());
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        if (loginAttemptService.isBlocked(email)) {
            throw new UnauthorizedException("Too many failed attempts. Try again in 15 minutes");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        // same message for unknown email and wrong password -> no user enumeration
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            loginAttemptService.recordFailure(email);
            throw new UnauthorizedException(BAD_CREDENTIALS);
        }
        if (!user.isActive()) {
            throw new UnauthorizedException("This account has been deactivated");
        }
        loginAttemptService.reset(email);
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                jwtService.getExpiresInSeconds(), EntityMapper.toResponse(user));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
