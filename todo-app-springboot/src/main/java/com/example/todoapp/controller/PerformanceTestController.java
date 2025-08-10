package com.example.todoapp.controller;

import com.example.todoapp.service.PerformanceMonitoringService;
import com.example.todoapp.service.TodoService;
import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Performance Testing Controller
 * 
 * This controller provides endpoints for performance testing and benchmarking:
 * - Load testing capabilities
 * - Performance metrics collection
 * - Benchmark operations
 * - Cache performance testing
 * - Database performance analysis
 * 
 * Available only in development profile for safety
 * 
 * @author System
 */
@RestController
@RequestMapping("/dev/performance")
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class PerformanceTestController {
    
    private final TodoService todoService;
    private final PerformanceMonitoringService performanceService;
    
    /**
     * Run performance benchmark test
     * Creates multiple todos and measures performance
     * 
     * @param count number of todos to create for testing
     * @return performance benchmark results
     */
    @PostMapping("/benchmark/create/{count}")
    public ResponseEntity<Map<String, Object>> runCreateBenchmark(@PathVariable int count) {
        log.info("Starting create benchmark test with {} todos", count);
        
        if (count > 10000) {
            count = 10000; // Safety limit
        }
        
        Map<String, Object> results = new HashMap<>();
        List<Long> durations = new ArrayList<>();
        
        long totalStartTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            Timer.Sample sample = performanceService.startTodoCreateTimer();
            long startTime = System.nanoTime();
            
            TodoRequest request = new TodoRequest();
            request.setTitle("Performance Test Todo " + (i + 1));
            request.setDescription("Created for performance testing purposes");
            request.setStatus(TodoStatus.TODO);
            request.setPriority(TodoPriority.MEDIUM);
            request.setDueDate(LocalDate.now().plusDays(7));
            
            try {
                todoService.create(request);
                long endTime = System.nanoTime();
                durations.add(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
                
                performanceService.stopTodoCreateTimer(sample);
                performanceService.recordTodoCreated();
                
            } catch (Exception e) {
                log.error("Error during benchmark test at iteration {}: {}", i, e.getMessage());
                break;
            }
        }
        
        long totalEndTime = System.currentTimeMillis();
        long totalDuration = totalEndTime - totalStartTime;
        
        // Calculate statistics
        OptionalDouble avgDuration = durations.stream().mapToLong(Long::longValue).average();
        long minDuration = durations.stream().mapToLong(Long::longValue).min().orElse(0L);
        long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0L);
        double throughput = (double) count / (totalDuration / 1000.0);
        
        results.put("totalCount", count);
        results.put("successfulOperations", durations.size());
        results.put("totalDuration", totalDuration + " ms");
        results.put("averageDuration", String.format("%.2f ms", avgDuration.orElse(0.0)));
        results.put("minDuration", minDuration + " ms");
        results.put("maxDuration", maxDuration + " ms");
        results.put("throughput", String.format("%.2f operations/second", throughput));
        
        log.info("Create benchmark completed: {} operations in {} ms", durations.size(), totalDuration);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Test cache performance
     * Measures cache hit/miss ratios and performance
     * 
     * @param iterations number of test iterations
     * @return cache performance results
     */
    @PostMapping("/benchmark/cache/{iterations}")
    public ResponseEntity<Map<String, Object>> testCachePerformance(@PathVariable int iterations) {
        log.info("Starting cache performance test with {} iterations", iterations);
        
        if (iterations > 5000) {
            iterations = 5000; // Safety limit
        }
        
        Map<String, Object> results = new HashMap<>();
        List<Long> cacheTimes = new ArrayList<>();
        List<Long> dbTimes = new ArrayList<>();
        
        // First, create some test data
        List<Long> testIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Cache Test Todo " + (i + 1));
            request.setDescription("Created for cache testing");
            request.setStatus(TodoStatus.TODO);
            request.setPriority(TodoPriority.MEDIUM);
            
            try {
                var todo = todoService.create(request);
                testIds.add(todo.getId());
            } catch (Exception e) {
                log.error("Error creating test data: {}", e.getMessage());
            }
        }
        
        // Test cache performance
        Random random = new Random();
        for (int i = 0; i < iterations; i++) {
            Long testId = testIds.get(random.nextInt(testIds.size()));
            
            long startTime = System.nanoTime();
            try {
                todoService.findById(testId);
                long endTime = System.nanoTime();
                cacheTimes.add(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
            } catch (Exception e) {
                log.warn("Error during cache test iteration {}: {}", i, e.getMessage());
            }
        }
        
        // Calculate cache statistics
        OptionalDouble avgCacheTime = cacheTimes.stream().mapToLong(Long::longValue).average();
        long minCacheTime = cacheTimes.stream().mapToLong(Long::longValue).min().orElse(0L);
        long maxCacheTime = cacheTimes.stream().mapToLong(Long::longValue).max().orElse(0L);
        
        results.put("totalIterations", iterations);
        results.put("successfulOperations", cacheTimes.size());
        results.put("averageResponseTime", String.format("%.2f ms", avgCacheTime.orElse(0.0)));
        results.put("minResponseTime", minCacheTime + " ms");
        results.put("maxResponseTime", maxCacheTime + " ms");
        results.put("cachePerformance", performanceService.getCachePerformanceDetails());
        
        log.info("Cache performance test completed: {} operations", cacheTimes.size());
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Run concurrent load test
     * Tests application performance under concurrent load
     * 
     * @param concurrency number of concurrent threads
     * @param operationsPerThread operations per thread
     * @return load test results
     */
    @PostMapping("/benchmark/load")
    public ResponseEntity<Map<String, Object>> runLoadTest(
            @RequestParam(defaultValue = "5") int concurrency,
            @RequestParam(defaultValue = "100") int operationsPerThread) {
        
        log.info("Starting load test with {} threads and {} operations per thread", 
                concurrency, operationsPerThread);
        
        // Safety limits
        if (concurrency > 10) concurrency = 10;
        if (operationsPerThread > 1000) operationsPerThread = 1000;
        
        final int finalConcurrency = concurrency;
        final int finalOperationsPerThread = operationsPerThread;
        
        Map<String, Object> results = new HashMap<>();
        List<CompletableFuture<Map<String, Object>>> futures = new ArrayList<>();
        
        long totalStartTime = System.currentTimeMillis();
        
        for (int threadIndex = 0; threadIndex < finalConcurrency; threadIndex++) {
            final int threadId = threadIndex;
            CompletableFuture<Map<String, Object>> future = CompletableFuture.supplyAsync(() -> {
                Map<String, Object> threadResults = new HashMap<>();
                List<Long> durations = new ArrayList<>();
                int successfulOps = 0;
                
                for (int i = 0; i < finalOperationsPerThread; i++) {
                    long startTime = System.nanoTime();
                    
                    TodoRequest request = new TodoRequest();
                    request.setTitle(String.format("Load Test Todo T%d-Op%d", threadId, i));
                    request.setDescription("Created during load testing");
                    request.setStatus(TodoStatus.TODO);
                    request.setPriority(TodoPriority.LOW);
                    
                    try {
                        todoService.create(request);
                        long endTime = System.nanoTime();
                        durations.add(TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
                        successfulOps++;
                    } catch (Exception e) {
                        log.warn("Error in thread {} operation {}: {}", threadId, i, e.getMessage());
                    }
                }
                
                OptionalDouble avgDuration = durations.stream().mapToLong(Long::longValue).average();
                threadResults.put("threadId", threadId);
                threadResults.put("successfulOperations", successfulOps);
                threadResults.put("averageDuration", avgDuration.orElse(0.0));
                
                return threadResults;
            });
            
            futures.add(future);
        }
        
        // Wait for all threads to complete
        List<Map<String, Object>> threadResults = new ArrayList<>();
        int totalSuccessfulOps = 0;
        
        try {
            for (CompletableFuture<Map<String, Object>> future : futures) {
                Map<String, Object> threadResult = future.get();
                threadResults.add(threadResult);
                totalSuccessfulOps += (Integer) threadResult.get("successfulOperations");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error waiting for load test completion: {}", e.getMessage());
        }
        
        long totalEndTime = System.currentTimeMillis();
        long totalDuration = totalEndTime - totalStartTime;
        double totalThroughput = (double) totalSuccessfulOps / (totalDuration / 1000.0);
        
        results.put("concurrency", finalConcurrency);
        results.put("operationsPerThread", finalOperationsPerThread);
        results.put("totalOperations", finalConcurrency * finalOperationsPerThread);
        results.put("successfulOperations", totalSuccessfulOps);
        results.put("totalDuration", totalDuration + " ms");
        results.put("throughput", String.format("%.2f operations/second", totalThroughput));
        results.put("threadResults", threadResults);
        
        log.info("Load test completed: {}/{} operations in {} ms", 
                totalSuccessfulOps, finalConcurrency * finalOperationsPerThread, totalDuration);
        
        return ResponseEntity.ok(results);
    }
    
    /**
     * Get current performance metrics
     * 
     * @return current performance statistics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getCurrentMetrics() {
        log.info("Retrieving current performance metrics");
        
        Map<String, Object> metrics = performanceService.getPerformanceStatistics();
        
        // Add JVM metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmMetrics = new HashMap<>();
        jvmMetrics.put("totalMemory", runtime.totalMemory());
        jvmMetrics.put("freeMemory", runtime.freeMemory());
        jvmMetrics.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        jvmMetrics.put("maxMemory", runtime.maxMemory());
        jvmMetrics.put("availableProcessors", runtime.availableProcessors());
        
        metrics.put("jvm", jvmMetrics);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Generate performance report
     * Creates a comprehensive performance analysis
     * 
     * @return detailed performance report
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> generatePerformanceReport() {
        log.info("Generating comprehensive performance report");
        
        Map<String, Object> report = new HashMap<>();
        
        // Get current metrics
        Map<String, Object> currentMetrics = performanceService.getPerformanceStatistics();
        report.put("currentMetrics", currentMetrics);
        
        // Get cache details
        Map<String, Object> cacheDetails = performanceService.getCachePerformanceDetails();
        report.put("cacheDetails", cacheDetails);
        
        // System information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("javaVendor", System.getProperty("java.vendor"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        report.put("systemInfo", systemInfo);
        
        // Performance recommendations
        List<String> recommendations = generateRecommendations(currentMetrics);
        report.put("recommendations", recommendations);
        
        // Report generation timestamp
        report.put("generatedAt", System.currentTimeMillis());
        report.put("generatedAtReadable", new Date().toString());
        
        performanceService.logPerformanceSummary();
        
        return ResponseEntity.ok(report);
    }
    
    private List<String> generateRecommendations(Map<String, Object> metrics) {
        List<String> recommendations = new ArrayList<>();
        
        // Cache hit ratio recommendations
        Double cacheHitRatio = (Double) metrics.get("cache.hit.ratio");
        if (cacheHitRatio != null && cacheHitRatio < 0.7) {
            recommendations.add("Consider increasing cache size or TTL - current hit ratio is " + 
                             String.format("%.1f%%", cacheHitRatio * 100));
        }
        
        // Performance timing recommendations
        Double avgCreateTime = (Double) metrics.get("todo.create.duration.avg");
        if (avgCreateTime != null && avgCreateTime > 100) {
            recommendations.add("Todo creation time is high (" + String.format("%.2f", avgCreateTime) + 
                             " ms) - consider database optimization");
        }
        
        Double avgSearchTime = (Double) metrics.get("todo.search.duration.avg");
        if (avgSearchTime != null && avgSearchTime > 200) {
            recommendations.add("Search operations are slow (" + String.format("%.2f", avgSearchTime) + 
                             " ms) - consider adding indexes or optimizing queries");
        }
        
        // Memory recommendations
        Runtime runtime = Runtime.getRuntime();
        double memoryUsageRatio = (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory();
        if (memoryUsageRatio > 0.8) {
            recommendations.add("High memory usage detected (" + String.format("%.1f%%", memoryUsageRatio * 100) + 
                             ") - consider increasing heap size or optimizing memory usage");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Performance metrics look good! No specific recommendations at this time.");
        }
        
        return recommendations;
    }
}