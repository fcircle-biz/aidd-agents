package com.example.todoapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Development environment profiler configuration.
 * Provides additional monitoring and profiling tools for development.
 */
@Configuration
@Profile("dev")
public class DevProfilerConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Timer for tracking database operations performance
     */
    @Bean
    public Timer databaseOperationTimer() {
        return Timer.builder("dev.database.operations")
                .description("Development database operation timing")
                .register(meterRegistry);
    }

    /**
     * Timer for tracking API response times
     */
    @Bean
    public Timer apiResponseTimer() {
        return Timer.builder("dev.api.response")
                .description("Development API response timing")
                .register(meterRegistry);
    }

    /**
     * Timer for tracking service layer operations
     */
    @Bean
    public Timer serviceOperationTimer() {
        return Timer.builder("dev.service.operations")
                .description("Development service operation timing")
                .register(meterRegistry);
    }
}