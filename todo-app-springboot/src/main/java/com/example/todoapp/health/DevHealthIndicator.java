package com.example.todoapp.health;

import com.example.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;

/**
 * Development environment specific health indicator.
 * Provides additional health checks and diagnostics for development.
 */
@Component("devHealth")
@Profile("dev")
public class DevHealthIndicator implements HealthIndicator {

    @Autowired
    private TodoRepository todoRepository;

    @Override
    public Health health() {
        try {
            // Database connectivity check
            long todoCount = todoRepository.count();
            
            // Memory check
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsagePercent = (double) heapUsed / heapMax * 100;
            
            // Thread count check
            int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
            
            // Build health status
            Health.Builder healthBuilder = Health.up()
                    .withDetail("database", "Connected")
                    .withDetail("todoCount", todoCount)
                    .withDetail("memoryUsagePercent", String.format("%.2f%%", memoryUsagePercent))
                    .withDetail("heapUsedMB", heapUsed / 1024 / 1024)
                    .withDetail("heapMaxMB", heapMax / 1024 / 1024)
                    .withDetail("threadCount", threadCount)
                    .withDetail("checkTime", LocalDateTime.now())
                    .withDetail("environment", "Development");
            
            // Add warnings for high resource usage
            if (memoryUsagePercent > 80) {
                healthBuilder.withDetail("memoryWarning", "Memory usage is high");
            }
            
            if (threadCount > 100) {
                healthBuilder.withDetail("threadWarning", "Thread count is high");
            }
            
            return healthBuilder.build();
            
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("checkTime", LocalDateTime.now())
                    .withDetail("environment", "Development")
                    .build();
        }
    }
}