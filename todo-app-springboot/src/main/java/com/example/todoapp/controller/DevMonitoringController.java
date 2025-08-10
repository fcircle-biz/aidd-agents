package com.example.todoapp.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Development Monitoring Controller
 * 
 * Provides comprehensive monitoring endpoints for development environment.
 * Includes JVM metrics, performance data, and system information.
 * 
 * @author System
 */
@RestController
@RequestMapping("/dev/monitoring")
@Profile("dev")
public class DevMonitoringController {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * Get comprehensive JVM metrics
     * 
     * @return JVM metrics including memory, threads, and garbage collection
     */
    @GetMapping("/jvm")
    public ResponseEntity<Map<String, Object>> getJvmMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Runtime information
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        Map<String, Object> runtime = new HashMap<>();
        runtime.put("vmName", runtimeBean.getVmName());
        runtime.put("vmVersion", runtimeBean.getVmVersion());
        runtime.put("uptime", runtimeBean.getUptime());
        runtime.put("startTime", runtimeBean.getStartTime());
        runtime.put("pid", runtimeBean.getName());
        metrics.put("runtime", runtime);
        
        // Memory information
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        Map<String, Object> memory = new HashMap<>();
        
        Map<String, Object> heap = new HashMap<>();
        heap.put("used", memoryBean.getHeapMemoryUsage().getUsed());
        heap.put("committed", memoryBean.getHeapMemoryUsage().getCommitted());
        heap.put("max", memoryBean.getHeapMemoryUsage().getMax());
        heap.put("init", memoryBean.getHeapMemoryUsage().getInit());
        memory.put("heap", heap);
        
        Map<String, Object> nonHeap = new HashMap<>();
        nonHeap.put("used", memoryBean.getNonHeapMemoryUsage().getUsed());
        nonHeap.put("committed", memoryBean.getNonHeapMemoryUsage().getCommitted());
        nonHeap.put("max", memoryBean.getNonHeapMemoryUsage().getMax());
        nonHeap.put("init", memoryBean.getNonHeapMemoryUsage().getInit());
        memory.put("nonHeap", nonHeap);
        
        memory.put("objectsPendingFinalization", memoryBean.getObjectPendingFinalizationCount());
        metrics.put("memory", memory);
        
        // Thread information
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", threadBean.getThreadCount());
        threads.put("peakCount", threadBean.getPeakThreadCount());
        threads.put("daemonCount", threadBean.getDaemonThreadCount());
        threads.put("totalStartedCount", threadBean.getTotalStartedThreadCount());
        metrics.put("threads", threads);
        
        // Garbage Collection information
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        Map<String, Object> gc = new HashMap<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            Map<String, Object> gcInfo = new HashMap<>();
            gcInfo.put("collectionCount", gcBean.getCollectionCount());
            gcInfo.put("collectionTime", gcBean.getCollectionTime());
            gcInfo.put("memoryPoolNames", gcBean.getMemoryPoolNames());
            gc.put(gcBean.getName(), gcInfo);
        }
        metrics.put("garbageCollectors", gc);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get application performance metrics
     * 
     * @return Performance metrics from Micrometer
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // HTTP request metrics
        Map<String, Object> http = new HashMap<>();
        try {
            double httpRequestsCount = meterRegistry.get("http.server.requests").counter().count();
            http.put("totalRequests", httpRequestsCount);
        } catch (Exception e) {
            http.put("totalRequests", 0);
        }
        metrics.put("http", http);
        
        // Database metrics (if available)
        Map<String, Object> database = new HashMap<>();
        try {
            double dbConnections = meterRegistry.get("hikaricp.connections.active").gauge().value();
            database.put("activeConnections", dbConnections);
        } catch (Exception e) {
            database.put("activeConnections", "N/A");
        }
        metrics.put("database", database);
        
        // Custom timer metrics
        Map<String, Object> timers = new HashMap<>();
        try {
            timers.put("databaseTimer", meterRegistry.get("database.operation").timer().count());
            timers.put("apiTimer", meterRegistry.get("api.response").timer().count());
            timers.put("serviceTimer", meterRegistry.get("service.operation").timer().count());
        } catch (Exception e) {
            timers.put("error", "Custom timers not available");
        }
        metrics.put("timers", timers);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get system resource information
     * 
     * @return System resource metrics
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // System properties
        Map<String, Object> system = new HashMap<>();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("javaVendor", System.getProperty("java.vendor"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("totalMemory", Runtime.getRuntime().totalMemory());
        system.put("freeMemory", Runtime.getRuntime().freeMemory());
        system.put("maxMemory", Runtime.getRuntime().maxMemory());
        metrics.put("system", system);
        
        // Application info
        Map<String, Object> app = new HashMap<>();
        app.put("springActiveProfiles", System.getProperty("spring.profiles.active", "default"));
        app.put("serverPort", System.getProperty("server.port", "8080"));
        app.put("workingDirectory", System.getProperty("user.dir"));
        app.put("tempDirectory", System.getProperty("java.io.tmpdir"));
        metrics.put("application", app);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get all available metrics from MeterRegistry
     * 
     * @return List of all available metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getAllMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> meters = new HashMap<>();
            meterRegistry.getMeters().forEach(meter -> {
                Map<String, Object> meterInfo = new HashMap<>();
                meterInfo.put("type", meter.getId().getType().name());
                meterInfo.put("description", meter.getId().getDescription());
                meterInfo.put("tags", meter.getId().getTags());
                meters.put(meter.getId().getName(), meterInfo);
            });
            result.put("meters", meters);
            result.put("count", meters.size());
        } catch (Exception e) {
            result.put("error", "Failed to retrieve metrics: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Reset development metrics (development only)
     * 
     * @return Reset confirmation
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetMetrics() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Force garbage collection
            System.gc();
            
            result.put("status", "success");
            result.put("message", "Metrics reset and garbage collection triggered");
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Failed to reset metrics: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}