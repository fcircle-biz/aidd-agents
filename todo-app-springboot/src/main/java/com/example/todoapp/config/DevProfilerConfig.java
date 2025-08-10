package com.example.todoapp.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Development Profiler Configuration
 * 
 * This configuration provides enhanced performance monitoring tools
 * for development environment, including custom timers and metrics.
 * 
 * @author System
 */
@Configuration
@Profile("dev")
public class DevProfilerConfig {

    /**
     * Database operation timer for development profiling
     * 
     * @param meterRegistry the meter registry
     * @return Timer for database operations
     */
    @Bean
    public Timer databaseTimer(MeterRegistry meterRegistry) {
        return Timer.builder("database.operation")
                .description("Timer for database operations")
                .tag("environment", "development")
                .register(meterRegistry);
    }

    /**
     * API response timer for development profiling
     * 
     * @param meterRegistry the meter registry
     * @return Timer for API responses
     */
    @Bean
    public Timer apiTimer(MeterRegistry meterRegistry) {
        return Timer.builder("api.response")
                .description("Timer for API response times")
                .tag("environment", "development")
                .register(meterRegistry);
    }

    /**
     * Service layer timer for development profiling
     * 
     * @param meterRegistry the meter registry
     * @return Timer for service operations
     */
    @Bean
    public Timer serviceTimer(MeterRegistry meterRegistry) {
        return Timer.builder("service.operation")
                .description("Timer for service layer operations")
                .tag("environment", "development")
                .register(meterRegistry);
    }
}