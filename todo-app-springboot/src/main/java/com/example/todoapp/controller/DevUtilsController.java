package com.example.todoapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Development utilities controller.
 * Provides various debugging and development utilities.
 */
@RestController
@RequestMapping("/dev/utils")
@Profile("dev")
public class DevUtilsController {

    private static final Logger logger = LoggerFactory.getLogger(DevUtilsController.class);

    /**
     * Test logging at different levels
     */
    @PostMapping("/test-logging/{level}")
    public ResponseEntity<Map<String, Object>> testLogging(@PathVariable String level, 
                                                          @RequestBody(required = false) Map<String, Object> message) {
        String testMessage = message != null && message.containsKey("message") 
                           ? message.get("message").toString() 
                           : "Development test log message";
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("level", level.toUpperCase());
        response.put("message", testMessage);
        
        switch (level.toLowerCase()) {
            case "trace":
                logger.trace("DEV TEST TRACE: {}", testMessage);
                break;
            case "debug":
                logger.debug("DEV TEST DEBUG: {}", testMessage);
                break;
            case "info":
                logger.info("DEV TEST INFO: {}", testMessage);
                break;
            case "warn":
                logger.warn("DEV TEST WARN: {}", testMessage);
                break;
            case "error":
                logger.error("DEV TEST ERROR: {}", testMessage);
                break;
            default:
                response.put("error", "Invalid log level. Use: trace, debug, info, warn, error");
                return ResponseEntity.badRequest().body(response);
        }
        
        response.put("status", "Log message sent successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Generate test exception for error handling testing
     */
    @PostMapping("/test-exception/{type}")
    public ResponseEntity<Map<String, Object>> testException(@PathVariable String type) {
        logger.info("Testing exception type: {}", type);
        
        switch (type.toLowerCase()) {
            case "runtime":
                throw new RuntimeException("Development test runtime exception");
            case "illegal":
                throw new IllegalArgumentException("Development test illegal argument exception");
            case "null":
                throw new NullPointerException("Development test null pointer exception");
            case "arithmetic":
                throw new ArithmeticException("Development test arithmetic exception");
            case "state":
                throw new IllegalStateException("Development test illegal state exception");
            default:
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Unknown exception type");
                error.put("availableTypes", Arrays.asList("runtime", "illegal", "null", "arithmetic", "state"));
                return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Memory usage stress test (for development only)
     */
    @PostMapping("/stress-test/memory/{sizeMB}")
    public ResponseEntity<Map<String, Object>> memoryStressTest(@PathVariable int sizeMB) {
        if (sizeMB <= 0 || sizeMB > 100) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Size must be between 1 and 100 MB");
            return ResponseEntity.badRequest().body(error);
        }
        
        logger.info("Starting memory stress test with {} MB", sizeMB);
        
        Map<String, Object> result = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        
        // Capture initial memory state
        long initialUsed = runtime.totalMemory() - runtime.freeMemory();
        result.put("initialUsedMemoryMB", initialUsed / 1024 / 1024);
        
        try {
            // Allocate memory
            List<byte[]> memoryBlocks = new ArrayList<>();
            for (int i = 0; i < sizeMB; i++) {
                memoryBlocks.add(new byte[1024 * 1024]); // 1MB blocks
            }
            
            // Capture peak memory state
            long peakUsed = runtime.totalMemory() - runtime.freeMemory();
            result.put("peakUsedMemoryMB", peakUsed / 1024 / 1024);
            result.put("allocatedMB", sizeMB);
            
            // Clear allocated memory
            memoryBlocks.clear();
            System.gc(); // Suggest garbage collection
            
            // Wait a moment for GC
            Thread.sleep(1000);
            
            // Capture final memory state
            long finalUsed = runtime.totalMemory() - runtime.freeMemory();
            result.put("finalUsedMemoryMB", finalUsed / 1024 / 1024);
            result.put("memoryRecoveredMB", (peakUsed - finalUsed) / 1024 / 1024);
            
            result.put("status", "Memory stress test completed successfully");
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("error", "Memory stress test failed: " + e.getMessage());
            logger.error("Memory stress test failed", e);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * CPU stress test (for development only)
     */
    @PostMapping("/stress-test/cpu/{durationSeconds}")
    public ResponseEntity<Map<String, Object>> cpuStressTest(@PathVariable int durationSeconds) {
        if (durationSeconds <= 0 || durationSeconds > 30) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Duration must be between 1 and 30 seconds");
            return ResponseEntity.badRequest().body(error);
        }
        
        logger.info("Starting CPU stress test for {} seconds", durationSeconds);
        
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (durationSeconds * 1000);
        
        try {
            long iterations = 0;
            while (System.currentTimeMillis() < endTime) {
                // CPU-intensive calculation
                Math.sqrt(Math.random() * 1000000);
                iterations++;
            }
            
            long actualDuration = System.currentTimeMillis() - startTime;
            result.put("status", "CPU stress test completed successfully");
            result.put("requestedDurationSeconds", durationSeconds);
            result.put("actualDurationMs", actualDuration);
            result.put("iterations", iterations);
            result.put("iterationsPerSecond", iterations / (actualDuration / 1000.0));
            result.put("timestamp", LocalDateTime.now());
            
        } catch (Exception e) {
            result.put("error", "CPU stress test failed: " + e.getMessage());
            logger.error("CPU stress test failed", e);
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * Generate sample data for various testing scenarios
     */
    @GetMapping("/generate-data/{type}")
    public ResponseEntity<Map<String, Object>> generateTestData(@PathVariable String type) {
        Map<String, Object> data = new HashMap<>();
        
        switch (type.toLowerCase()) {
            case "json":
                data.put("sampleObject", createSampleJsonObject());
                break;
            case "array":
                data.put("sampleArray", createSampleArray());
                break;
            case "nested":
                data.put("sampleNested", createNestedObject());
                break;
            case "unicode":
                data.put("sampleUnicode", createUnicodeData());
                break;
            case "large":
                data.put("sampleLarge", createLargeDataSet(100));
                break;
            default:
                data.put("error", "Unknown data type");
                data.put("availableTypes", Arrays.asList("json", "array", "nested", "unicode", "large"));
                return ResponseEntity.badRequest().body(data);
        }
        
        data.put("timestamp", LocalDateTime.now());
        data.put("type", type);
        
        return ResponseEntity.ok(data);
    }

    /**
     * Environment information for debugging
     */
    @GetMapping("/environment-info")
    public ResponseEntity<Map<String, Object>> getEnvironmentInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // JVM information
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("vendor", System.getProperty("java.vendor"));
        jvm.put("home", System.getProperty("java.home"));
        jvm.put("vmName", System.getProperty("java.vm.name"));
        jvm.put("vmVersion", System.getProperty("java.vm.version"));
        info.put("jvm", jvm);
        
        // System information
        Map<String, Object> system = new HashMap<>();
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        system.put("userDir", System.getProperty("user.dir"));
        system.put("userTimezone", System.getProperty("user.timezone"));
        info.put("system", system);
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemoryMB", runtime.totalMemory() / 1024 / 1024);
        memory.put("freeMemoryMB", runtime.freeMemory() / 1024 / 1024);
        memory.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        memory.put("maxMemoryMB", runtime.maxMemory() / 1024 / 1024);
        info.put("memory", memory);
        
        info.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }

    private Map<String, Object> createSampleJsonObject() {
        Map<String, Object> obj = new HashMap<>();
        obj.put("id", 1);
        obj.put("name", "サンプルオブジェクト");
        obj.put("active", true);
        obj.put("value", 42.5);
        obj.put("tags", Arrays.asList("tag1", "tag2", "tag3"));
        return obj;
    }

    private List<Map<String, Object>> createSampleArray() {
        List<Map<String, Object>> array = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("name", "アイテム " + i);
            item.put("value", i * 10);
            array.add(item);
        }
        return array;
    }

    private Map<String, Object> createNestedObject() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("level1", Map.of(
            "level2", Map.of(
                "level3", Map.of(
                    "deepValue", "深くネストされた値",
                    "deepNumber", 123
                ),
                "level3Array", Arrays.asList("a", "b", "c")
            ),
            "level2Direct", "直接の値"
        ));
        return nested;
    }

    private Map<String, Object> createUnicodeData() {
        Map<String, Object> unicode = new HashMap<>();
        unicode.put("japanese", "こんにちは世界 🌍");
        unicode.put("chinese", "你好世界 🌏");
        unicode.put("korean", "안녕하세요 세계 🌎");
        unicode.put("emoji", "😀 😃 😄 😁 😆 😅 😂 🤣");
        unicode.put("symbols", "★ ☆ ♠ ♣ ♥ ♦ ♪ ♫ ☎ ✉");
        return unicode;
    }

    private List<Map<String, Object>> createLargeDataSet(int size) {
        List<Map<String, Object>> largeSet = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("name", "大容量データ項目 " + i);
            item.put("description", "これは大容量データセットのテスト用項目です。項目番号: " + i);
            item.put("timestamp", LocalDateTime.now().minusHours(i));
            item.put("randomValue", Math.random() * 1000);
            largeSet.add(item);
        }
        return largeSet;
    }
}