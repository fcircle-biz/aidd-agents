package com.example.todoapp.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Performance Monitoring Service
 * 
 * Provides comprehensive performance monitoring capabilities including
 * execution time tracking, resource usage monitoring, and performance alerts.
 * 
 * @author System
 */
@Service
@Profile({"dev", "prod"})
public class PerformanceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringService.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("performance");

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Monitor method execution time and log performance metrics
     * 
     * @param methodName the method name being monitored
     * @param className the class name
     * @param executionTime the execution time in milliseconds
     */
    public void recordExecutionTime(String className, String methodName, long executionTime) {
        String timerName = String.format("method.execution.%s.%s", className, methodName);
        
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder(timerName)
                .description("Method execution time")
                .tag("class", className)
                .tag("method", methodName)
                .register(meterRegistry));

        // Log performance data
        Map<String, Object> perfData = new HashMap<>();
        perfData.put("class", className);
        perfData.put("method", methodName);
        perfData.put("executionTime", executionTime);
        perfData.put("timestamp", System.currentTimeMillis());
        
        // Evaluate performance level
        String performanceLevel = evaluatePerformanceLevel(executionTime);
        perfData.put("performanceLevel", performanceLevel);
        
        performanceLogger.info("Method Performance: {}", perfData);
        
        // Alert for slow operations
        if (executionTime > 5000) { // 5 seconds
            logger.warn("Slow operation detected: {}.{} took {}ms", className, methodName, executionTime);
        }
    }

    /**
     * Record database operation performance
     * 
     * @param operation the database operation type
     * @param executionTime the execution time in milliseconds
     * @param recordCount the number of records processed
     */
    public void recordDatabaseOperation(String operation, long executionTime, int recordCount) {
        Timer dbTimer = Timer.builder("database.operation")
                .description("Database operation execution time")
                .tag("operation", operation)
                .register(meterRegistry);
        
        dbTimer.record(executionTime, TimeUnit.MILLISECONDS);
        
        Map<String, Object> dbPerfData = new HashMap<>();
        dbPerfData.put("operation", operation);
        dbPerfData.put("executionTime", executionTime);
        dbPerfData.put("recordCount", recordCount);
        dbPerfData.put("recordsPerSecond", recordCount > 0 ? (recordCount * 1000.0 / executionTime) : 0);
        dbPerfData.put("timestamp", System.currentTimeMillis());
        
        performanceLogger.info("Database Performance: {}", dbPerfData);
        
        // Alert for slow database operations
        if (executionTime > 1000) { // 1 second
            logger.warn("Slow database operation: {} took {}ms for {} records", operation, executionTime, recordCount);
        }
        
        // Alert for large dataset operations
        if (recordCount > 1000) {
            logger.info("Large dataset operation: {} processed {} records in {}ms", operation, recordCount, executionTime);
        }
    }

    /**
     * Record API call performance
     * 
     * @param endpoint the API endpoint
     * @param httpMethod the HTTP method
     * @param statusCode the response status code
     * @param executionTime the execution time in milliseconds
     */
    public void recordApiCall(String endpoint, String httpMethod, int statusCode, long executionTime) {
        Timer apiTimer = Timer.builder("api.call")
                .description("API call execution time")
                .tag("endpoint", endpoint)
                .tag("method", httpMethod)
                .tag("status", String.valueOf(statusCode))
                .register(meterRegistry);
        
        apiTimer.record(executionTime, TimeUnit.MILLISECONDS);
        
        Map<String, Object> apiPerfData = new HashMap<>();
        apiPerfData.put("endpoint", endpoint);
        apiPerfData.put("method", httpMethod);
        apiPerfData.put("statusCode", statusCode);
        apiPerfData.put("executionTime", executionTime);
        apiPerfData.put("timestamp", System.currentTimeMillis());
        
        performanceLogger.info("API Performance: {}", apiPerfData);
        
        // Alert for slow API calls
        if (executionTime > 2000) { // 2 seconds
            logger.warn("Slow API call: {} {} took {}ms", httpMethod, endpoint, executionTime);
        }
    }

    /**
     * Get current system performance snapshot
     * 
     * @return performance snapshot
     */
    public Map<String, Object> getPerformanceSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        
        // Memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", memoryBean.getHeapMemoryUsage().getUsed());
        memory.put("heapMax", memoryBean.getHeapMemoryUsage().getMax());
        memory.put("heapUsagePercent", 
            (double) memoryBean.getHeapMemoryUsage().getUsed() / memoryBean.getHeapMemoryUsage().getMax() * 100);
        snapshot.put("memory", memory);
        
        // Thread metrics
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", threadBean.getThreadCount());
        threads.put("peakCount", threadBean.getPeakThreadCount());
        threads.put("daemonCount", threadBean.getDaemonThreadCount());
        snapshot.put("threads", threads);
        
        // CPU metrics
        double cpuUsage = getCpuUsage();
        snapshot.put("cpuUsage", cpuUsage);
        
        // Performance evaluation
        String systemHealth = evaluateSystemHealth(memory, threads, cpuUsage);
        snapshot.put("systemHealth", systemHealth);
        snapshot.put("timestamp", System.currentTimeMillis());
        
        return snapshot;
    }

    /**
     * Start performance monitoring for a specific operation
     * 
     * @param operationName the operation name
     * @return performance monitor context
     */
    public PerformanceMonitorContext startMonitoring(String operationName) {
        return new PerformanceMonitorContext(operationName, System.currentTimeMillis());
    }

    /**
     * Evaluate performance level based on execution time
     * 
     * @param executionTime execution time in milliseconds
     * @return performance level
     */
    private String evaluatePerformanceLevel(long executionTime) {
        if (executionTime < 100) return "EXCELLENT";
        if (executionTime < 500) return "GOOD";
        if (executionTime < 1000) return "ACCEPTABLE";
        if (executionTime < 5000) return "POOR";
        return "CRITICAL";
    }

    /**
     * Evaluate overall system health
     */
    private String evaluateSystemHealth(Map<String, Object> memory, Map<String, Object> threads, double cpuUsage) {
        double heapUsagePercent = (Double) memory.get("heapUsagePercent");
        int threadCount = (Integer) threads.get("count");
        
        if (heapUsagePercent > 90 || cpuUsage > 90 || threadCount > 1000) {
            return "CRITICAL";
        } else if (heapUsagePercent > 80 || cpuUsage > 80 || threadCount > 500) {
            return "WARNING";
        } else if (heapUsagePercent > 70 || cpuUsage > 70 || threadCount > 200) {
            return "CAUTION";
        } else {
            return "HEALTHY";
        }
    }

    /**
     * Get CPU usage percentage
     * 
     * @return CPU usage percentage
     */
    private double getCpuUsage() {
        try {
            com.sun.management.OperatingSystemMXBean osBean = 
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            return osBean.getProcessCpuLoad() * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Performance Monitor Context
     */
    public class PerformanceMonitorContext {
        private final String operationName;
        private final long startTime;
        private final Map<String, Object> metadata;

        public PerformanceMonitorContext(String operationName, long startTime) {
            this.operationName = operationName;
            this.startTime = startTime;
            this.metadata = new HashMap<>();
        }

        public PerformanceMonitorContext addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }

        public void stop() {
            long executionTime = System.currentTimeMillis() - startTime;
            recordExecutionTime("Unknown", operationName, executionTime);
            
            Map<String, Object> contextData = new HashMap<>(metadata);
            contextData.put("operation", operationName);
            contextData.put("executionTime", executionTime);
            contextData.put("timestamp", System.currentTimeMillis());
            
            performanceLogger.info("Operation Performance: {}", contextData);
        }
    }
}