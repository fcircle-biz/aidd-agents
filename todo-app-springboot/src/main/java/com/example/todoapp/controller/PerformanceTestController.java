package com.example.todoapp.controller;

import com.example.todoapp.service.PerformanceMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Performance Test Controller
 * 
 * Provides endpoints for performance testing and load simulation.
 * Used for development and performance optimization testing.
 * 
 * @author System
 */
@RestController
@RequestMapping("/dev/performance")
@Profile("dev")
public class PerformanceTestController {

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    /**
     * CPU intensive performance test
     * 
     * @param iterations number of iterations for CPU test
     * @return performance test results
     */
    @PostMapping("/cpu-test")
    public ResponseEntity<Map<String, Object>> cpuTest(@RequestParam(defaultValue = "1000000") int iterations) {
        var monitor = performanceMonitoringService.startMonitoring("CPU_INTENSIVE_TEST");
        monitor.addMetadata("iterations", iterations);
        
        long startTime = System.currentTimeMillis();
        
        // CPU intensive calculation
        long result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        monitor.stop();
        
        Map<String, Object> response = new HashMap<>();
        response.put("testType", "CPU_INTENSIVE");
        response.put("iterations", iterations);
        response.put("executionTime", executionTime);
        response.put("iterationsPerSecond", iterations * 1000.0 / executionTime);
        response.put("result", result);
        response.put("performance", evaluatePerformance(executionTime));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Memory intensive performance test
     * 
     * @param sizeInMB size of memory to allocate in MB
     * @return performance test results
     */
    @PostMapping("/memory-test")
    public ResponseEntity<Map<String, Object>> memoryTest(@RequestParam(defaultValue = "50") int sizeInMB) {
        var monitor = performanceMonitoringService.startMonitoring("MEMORY_INTENSIVE_TEST");
        monitor.addMetadata("sizeInMB", sizeInMB);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Memory intensive allocation
            List<byte[]> memoryBlocks = new ArrayList<>();
            int blockSize = 1024 * 1024; // 1MB blocks
            
            for (int i = 0; i < sizeInMB; i++) {
                byte[] block = new byte[blockSize];
                // Fill with data to ensure actual allocation
                for (int j = 0; j < blockSize; j += 1024) {
                    block[j] = (byte) (j % 256);
                }
                memoryBlocks.add(block);
            }
            
            // Force memory access
            long checksum = 0;
            for (byte[] block : memoryBlocks) {
                checksum += block[0];
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            monitor.stop();
            
            Map<String, Object> response = new HashMap<>();
            response.put("testType", "MEMORY_INTENSIVE");
            response.put("allocatedMB", sizeInMB);
            response.put("executionTime", executionTime);
            response.put("throughputMBPS", sizeInMB * 1000.0 / executionTime);
            response.put("checksum", checksum);
            response.put("performance", evaluatePerformance(executionTime));
            
            // Cleanup
            memoryBlocks.clear();
            System.gc();
            
            return ResponseEntity.ok(response);
            
        } catch (OutOfMemoryError e) {
            monitor.stop();
            Map<String, Object> response = new HashMap<>();
            response.put("testType", "MEMORY_INTENSIVE");
            response.put("error", "OutOfMemoryError");
            response.put("requestedMB", sizeInMB);
            response.put("message", "Insufficient memory for test");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Concurrent load test
     * 
     * @param threadCount number of concurrent threads
     * @param taskDuration duration of each task in milliseconds
     * @return load test results
     */
    @PostMapping("/load-test")
    public ResponseEntity<Map<String, Object>> loadTest(
            @RequestParam(defaultValue = "10") int threadCount,
            @RequestParam(defaultValue = "1000") int taskDuration) {
        
        var monitor = performanceMonitoringService.startMonitoring("CONCURRENT_LOAD_TEST");
        monitor.addMetadata("threadCount", threadCount);
        monitor.addMetadata("taskDuration", taskDuration);
        
        long startTime = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // Create concurrent tasks
        for (int i = 0; i < threadCount; i++) {
            final int taskId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                long taskStart = System.currentTimeMillis();
                
                // Simulate work
                try {
                    Thread.sleep(taskDuration);
                    
                    // Add some CPU work
                    double result = 0;
                    for (int j = 0; j < 10000; j++) {
                        result += Math.sqrt(j * taskId);
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                return System.currentTimeMillis() - taskStart;
            }, executor);
            
            futures.add(future);
        }
        
        // Wait for all tasks to complete
        List<Long> taskTimes = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            try {
                taskTimes.add(future.get());
            } catch (Exception e) {
                taskTimes.add(-1L);
            }
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long totalExecutionTime = System.currentTimeMillis() - startTime;
        monitor.stop();
        
        // Calculate statistics
        double avgTaskTime = taskTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTaskTime = taskTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTaskTime = taskTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        
        Map<String, Object> response = new HashMap<>();
        response.put("testType", "CONCURRENT_LOAD");
        response.put("threadCount", threadCount);
        response.put("plannedTaskDuration", taskDuration);
        response.put("totalExecutionTime", totalExecutionTime);
        response.put("avgTaskTime", avgTaskTime);
        response.put("maxTaskTime", maxTaskTime);
        response.put("minTaskTime", minTaskTime);
        response.put("throughput", threadCount * 1000.0 / totalExecutionTime);
        response.put("performance", evaluatePerformance(totalExecutionTime));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Database simulation performance test
     * 
     * @param operationCount number of database operations to simulate
     * @return database performance test results
     */
    @PostMapping("/db-simulation")
    public ResponseEntity<Map<String, Object>> databaseSimulationTest(
            @RequestParam(defaultValue = "1000") int operationCount) {
        
        var monitor = performanceMonitoringService.startMonitoring("DATABASE_SIMULATION_TEST");
        monitor.addMetadata("operationCount", operationCount);
        
        long startTime = System.currentTimeMillis();
        
        // Simulate database operations with different latencies
        Map<String, Integer> operations = new HashMap<>();
        operations.put("SELECT", operationCount / 2);
        operations.put("INSERT", operationCount / 4);
        operations.put("UPDATE", operationCount / 8);
        operations.put("DELETE", operationCount / 8);
        
        Map<String, Long> operationTimes = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : operations.entrySet()) {
            String operation = entry.getKey();
            int count = entry.getValue();
            
            long opStartTime = System.currentTimeMillis();
            
            for (int i = 0; i < count; i++) {
                // Simulate different operation latencies
                try {
                    switch (operation) {
                        case "SELECT" -> Thread.sleep(1); // Fast reads
                        case "INSERT" -> Thread.sleep(3); // Medium writes
                        case "UPDATE" -> Thread.sleep(5); // Slower updates
                        case "DELETE" -> Thread.sleep(2); // Medium deletes
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            long opExecutionTime = System.currentTimeMillis() - opStartTime;
            operationTimes.put(operation, opExecutionTime);
            
            // Record individual operation performance
            performanceMonitoringService.recordDatabaseOperation(operation, opExecutionTime, count);
        }
        
        long totalExecutionTime = System.currentTimeMillis() - startTime;
        monitor.stop();
        
        Map<String, Object> response = new HashMap<>();
        response.put("testType", "DATABASE_SIMULATION");
        response.put("totalOperations", operationCount);
        response.put("operationBreakdown", operations);
        response.put("operationTimes", operationTimes);
        response.put("totalExecutionTime", totalExecutionTime);
        response.put("operationsPerSecond", operationCount * 1000.0 / totalExecutionTime);
        response.put("performance", evaluatePerformance(totalExecutionTime));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get current system performance snapshot
     * 
     * @return current performance metrics
     */
    @GetMapping("/snapshot")
    public ResponseEntity<Map<String, Object>> getPerformanceSnapshot() {
        Map<String, Object> snapshot = performanceMonitoringService.getPerformanceSnapshot();
        return ResponseEntity.ok(snapshot);
    }

    /**
     * Stress test combining multiple performance aspects
     * 
     * @return comprehensive stress test results
     */
    @PostMapping("/stress-test")
    public ResponseEntity<Map<String, Object>> stressTest() {
        var monitor = performanceMonitoringService.startMonitoring("COMPREHENSIVE_STRESS_TEST");
        
        long startTime = System.currentTimeMillis();
        Map<String, Object> results = new HashMap<>();
        
        // CPU stress
        long cpuStart = System.currentTimeMillis();
        double cpuResult = 0;
        for (int i = 0; i < 500000; i++) {
            cpuResult += Math.sqrt(i) * Math.sin(i);
        }
        long cpuTime = System.currentTimeMillis() - cpuStart;
        results.put("cpuTest", Map.of("executionTime", cpuTime, "result", cpuResult));
        
        // Memory stress
        long memStart = System.currentTimeMillis();
        List<int[]> memoryBlocks = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int[] block = new int[10000];
            for (int j = 0; j < block.length; j++) {
                block[j] = j * i;
            }
            memoryBlocks.add(block);
        }
        long memTime = System.currentTimeMillis() - memStart;
        results.put("memoryTest", Map.of("executionTime", memTime, "blocksAllocated", memoryBlocks.size()));
        
        // Cleanup
        memoryBlocks.clear();
        System.gc();
        
        long totalTime = System.currentTimeMillis() - startTime;
        monitor.stop();
        
        results.put("totalExecutionTime", totalTime);
        results.put("performance", evaluatePerformance(totalTime));
        results.put("systemHealth", performanceMonitoringService.getPerformanceSnapshot().get("systemHealth"));
        
        return ResponseEntity.ok(results);
    }

    /**
     * Evaluate performance based on execution time
     * 
     * @param executionTime execution time in milliseconds
     * @return performance evaluation
     */
    private String evaluatePerformance(long executionTime) {
        if (executionTime < 100) return "EXCELLENT";
        if (executionTime < 500) return "GOOD";
        if (executionTime < 1000) return "ACCEPTABLE";
        if (executionTime < 5000) return "POOR";
        return "CRITICAL";
    }
}