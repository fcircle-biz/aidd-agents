package com.example.todoapp.health;

import com.example.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Development environment specific health indicator.
 * Provides additional health checks and diagnostics for development.
 */
@Component("devHealth")
@Profile("dev")
public class DevHealthIndicator {

    @Autowired
    private TodoRepository todoRepository;

    public Map<String, Object> getHealthInfo() {
        Map<String, Object> healthInfo = new HashMap<>();
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
            healthInfo.put("status", "UP");
            healthInfo.put("database", "Connected");
            healthInfo.put("todoCount", todoCount);
            healthInfo.put("memoryUsagePercent", String.format("%.2f%%", memoryUsagePercent));
            healthInfo.put("heapUsedMB", heapUsed / 1024 / 1024);
            healthInfo.put("heapMaxMB", heapMax / 1024 / 1024);
            healthInfo.put("threadCount", threadCount);
            healthInfo.put("checkTime", LocalDateTime.now());
            healthInfo.put("environment", "Development");
            
            // Add warnings for high resource usage
            if (memoryUsagePercent > 80) {
                healthInfo.put("memoryWarning", "Memory usage is high");
            }
            
            if (threadCount > 100) {
                healthInfo.put("threadWarning", "Thread count is high");
            }
            
        } catch (Exception e) {
            healthInfo.put("status", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("checkTime", LocalDateTime.now());
            healthInfo.put("environment", "Development");
        }
        
        return healthInfo;
    }
}