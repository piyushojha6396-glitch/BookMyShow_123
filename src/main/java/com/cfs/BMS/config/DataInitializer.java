package com.cfs.BMS.config;

import com.cfs.BMS.entity.User;
import com.cfs.BMS.enums.Role;
import com.cfs.BMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first ADMIN account from configuration (APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD). */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AppProperties props;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = props.admin();
        if (admin.email() == null || admin.email().isBlank()
                || admin.password() == null || admin.password().isBlank()) {
            log.info("No bootstrap admin configured (app.admin.email / app.admin.password) - skipping");
            return;
        }
        String email = admin.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }
        userRepository.save(User.builder()
                .name(admin.name())
                .email(email)
                .password(passwordEncoder.encode(admin.password()))
                .role(Role.ADMIN)
                .active(true)
                .build());
        log.info("Bootstrap admin account created for {}", email);
    }
}
