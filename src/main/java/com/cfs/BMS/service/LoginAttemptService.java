package com.cfs.BMS.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory brute-force protection: after MAX_FAILURES wrong passwords the email is
 * locked for LOCK_DURATION. (For a multi-instance deployment move this state to Redis.)
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private record Attempt(int failures, Instant lockedUntil) {
    }

    private final Clock clock;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt a = attempts.get(key);
        if (a == null || a.lockedUntil() == null) {
            return false;
        }
        if (a.lockedUntil().isAfter(clock.instant())) {
            return true;
        }
        attempts.remove(key);
        return false;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (k, old) -> {
            int failures = (old == null ? 0 : old.failures()) + 1;
            Instant lock = failures >= MAX_FAILURES ? clock.instant().plus(LOCK_DURATION) : null;
            return new Attempt(failures, lock);
        });
    }

    public void reset(String key) {
        attempts.remove(key);
    }
}
