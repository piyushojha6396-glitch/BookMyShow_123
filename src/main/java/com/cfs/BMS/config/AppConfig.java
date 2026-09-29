package com.cfs.BMS.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /** Single time source for the whole app (easy to fix in tests, zone-aware in production). */
    @Bean
    public Clock clock(AppProperties props) {
        return Clock.system(ZoneId.of(props.timezone()));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
