package com.example.todoapp.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Development monitoring controller.
 * Provides detailed performance and system monitoring endpoints for development.
 */
@RestController
@RequestMapping("/dev/monitor")
@Profile("dev")
public class DevMonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(DevMonitoringController.class);

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Get JVM performance metrics
     */
    @GetMapping("/jvm")
    public ResponseEntity<Map<String, Object>> getJvmMetrics() {
        logger.debug("Retrieving JVM performance metrics");
        
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        Map<String, Object> metrics = new HashMap<>();
        
        // Runtime information
        Map<String, Object> runtime = new HashMap<>();
        runtime.put("uptime", runtimeBean.getUptime());
        runtime.put("startTime", runtimeBean.getStartTime());
        runtime.put("vmName", runtimeBean.getVmName());
        runtime.put("vmVersion", runtimeBean.getVmVersion());
        runtime.put("vmVendor", runtimeBean.getVmVendor());
        metrics.put("runtime", runtime);
        
        // Memory information
        Map<String, Object> memory = new HashMap<>();
        memory.put("heapUsed", memoryBean.getHeapMemoryUsage().getUsed());
        memory.put("heapMax", memoryBean.getHeapMemoryUsage().getMax());
        memory.put("heapCommitted", memoryBean.getHeapMemoryUsage().getCommitted());
        memory.put("nonHeapUsed", memoryBean.getNonHeapMemoryUsage().getUsed());
        memory.put("nonHeapMax", memoryBean.getNonHeapMemoryUsage().getMax());
        memory.put("nonHeapCommitted", memoryBean.getNonHeapMemoryUsage().getCommitted());
        metrics.put("memory", memory);
        
        // Thread information
        Map<String, Object> threads = new HashMap<>();
        threads.put("threadCount", threadBean.getThreadCount());
        threads.put("peakThreadCount", threadBean.getPeakThreadCount());
        threads.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        threads.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());
        metrics.put("threads", threads);
        
        // Garbage Collection information
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gcBean -> {
            Map<String, Object> gc = new HashMap<>();
            gc.put("collectionCount", gcBean.getCollectionCount());
            gc.put("collectionTime", gcBean.getCollectionTime());
            metrics.put("gc_" + gcBean.getName().replaceAll("\\s+", "_"), gc);
        });
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get application-specific performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        logger.debug("Retrieving application performance metrics");
        
        Map<String, Object> performance = new HashMap<>();
        
        // HTTP request metrics
        Map<String, Object> httpMetrics = new HashMap<>();
        try {
            double httpRequestsTotal = meterRegistry.get("http.server.requests").counter().count();
            httpMetrics.put("totalRequests", httpRequestsTotal);
            
            // Get request duration metrics if available
            try {
                double avgDuration = meterRegistry.get("http.server.requests").timer().mean(java.util.concurrent.TimeUnit.MILLISECONDS);
                double maxDuration = meterRegistry.get("http.server.requests").timer().max(java.util.concurrent.TimeUnit.MILLISECONDS);
                httpMetrics.put("avgResponseTime", avgDuration);
                httpMetrics.put("maxResponseTime", maxDuration);
            } catch (Exception e) {
                httpMetrics.put("avgResponseTime", "N/A");
                httpMetrics.put("maxResponseTime", "N/A");
            }
        } catch (Exception e) {
            httpMetrics.put("totalRequests", "N/A");
        }
        performance.put("http", httpMetrics);
        
        // Database metrics
        Map<String, Object> dbMetrics = new HashMap<>();
        try {
            double dbConnectionsActive = meterRegistry.get("hikaricp.connections.active").gauge().value();
            double dbConnectionsTotal = meterRegistry.get("hikaricp.connections").gauge().value();
            dbMetrics.put("activeConnections", dbConnectionsActive);
            dbMetrics.put("totalConnections", dbConnectionsTotal);
        } catch (Exception e) {
            dbMetrics.put("activeConnections", "N/A");
            dbMetrics.put("totalConnections", "N/A");
        }
        performance.put("database", dbMetrics);
        
        // JVM metrics from Micrometer
        Map<String, Object> jvmMetrics = new HashMap<>();
        try {
            double memoryUsed = meterRegistry.get("jvm.memory.used").gauge().value();
            double memoryMax = meterRegistry.get("jvm.memory.max").gauge().value();
            double cpuUsage = meterRegistry.get("process.cpu.usage").gauge().value();
            
            jvmMetrics.put("memoryUsedMB", memoryUsed / 1024 / 1024);
            jvmMetrics.put("memoryMaxMB", memoryMax / 1024 / 1024);
            jvmMetrics.put("memoryUsagePercent", (memoryUsed / memoryMax) * 100);
            jvmMetrics.put("cpuUsagePercent", cpuUsage * 100);
        } catch (Exception e) {
            jvmMetrics.put("error", "Metrics not available");
        }
        performance.put("jvm", jvmMetrics);
        
        return ResponseEntity.ok(performance);
    }

    /**
     * Get system resource usage
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        logger.debug("Retrieving system resource metrics");
        
        Map<String, Object> system = new HashMap<>();
        
        // Available processors
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("totalMemory", runtime.totalMemory());
        memoryInfo.put("freeMemory", runtime.freeMemory());
        memoryInfo.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        memoryInfo.put("maxMemory", runtime.maxMemory());
        system.put("runtime", memoryInfo);
        
        // Operating system information
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", System.getProperty("os.name"));
        osInfo.put("version", System.getProperty("os.version"));
        osInfo.put("architecture", System.getProperty("os.arch"));
        system.put("operatingSystem", osInfo);
        
        // Java information
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        javaInfo.put("home", System.getProperty("java.home"));
        system.put("java", javaInfo);
        
        return ResponseEntity.ok(system);
    }

    /**
     * Reset performance counters (development only)
     */
    @PostMapping("/reset-metrics")
    public ResponseEntity<Map<String, Object>> resetMetrics() {
        logger.info("Resetting development performance metrics");
        
        // This is a development-only feature to reset metrics
        // In a real production system, you would not want to reset metrics
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Metrics reset completed");
        result.put("note", "This operation is only available in development environment");
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get custom application metrics
     */
    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> getCustomMetrics() {
        logger.debug("Retrieving custom application metrics");
        
        Map<String, Object> custom = new HashMap<>();
        
        try {
            // Database operation timing
            double dbOpCount = meterRegistry.get("dev.database.operations").timer().count();
            double dbOpMean = meterRegistry.get("dev.database.operations").timer().mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            double dbOpMax = meterRegistry.get("dev.database.operations").timer().max(java.util.concurrent.TimeUnit.MILLISECONDS);
            
            Map<String, Object> dbOps = new HashMap<>();
            dbOps.put("count", dbOpCount);
            dbOps.put("meanTime", dbOpMean);
            dbOps.put("maxTime", dbOpMax);
            custom.put("databaseOperations", dbOps);
            
        } catch (Exception e) {
            custom.put("databaseOperations", "No data available");
        }
        
        try {
            // API response timing
            double apiCount = meterRegistry.get("dev.api.response").timer().count();
            double apiMean = meterRegistry.get("dev.api.response").timer().mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            double apiMax = meterRegistry.get("dev.api.response").timer().max(java.util.concurrent.TimeUnit.MILLISECONDS);
            
            Map<String, Object> apiOps = new HashMap<>();
            apiOps.put("count", apiCount);
            apiOps.put("meanTime", apiMean);
            apiOps.put("maxTime", apiMax);
            custom.put("apiOperations", apiOps);
            
        } catch (Exception e) {
            custom.put("apiOperations", "No data available");
        }
        
        try {
            // Service operation timing
            double serviceCount = meterRegistry.get("dev.service.operations").timer().count();
            double serviceMean = meterRegistry.get("dev.service.operations").timer().mean(java.util.concurrent.TimeUnit.MILLISECONDS);
            double serviceMax = meterRegistry.get("dev.service.operations").timer().max(java.util.concurrent.TimeUnit.MILLISECONDS);
            
            Map<String, Object> serviceOps = new HashMap<>();
            serviceOps.put("count", serviceCount);
            serviceOps.put("meanTime", serviceMean);
            serviceOps.put("maxTime", serviceMax);
            custom.put("serviceOperations", serviceOps);
            
        } catch (Exception e) {
            custom.put("serviceOperations", "No data available");
        }
        
        return ResponseEntity.ok(custom);
    }
}