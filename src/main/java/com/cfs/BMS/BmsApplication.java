package com.cfs.BMS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmsApplication.class, args);
    }
}
