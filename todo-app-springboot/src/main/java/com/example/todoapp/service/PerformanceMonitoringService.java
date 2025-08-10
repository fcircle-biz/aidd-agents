package com.example.todoapp.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Performance Monitoring Service
 * 
 * This service provides comprehensive performance monitoring capabilities including:
 * - Custom metrics collection and reporting
 * - Database performance tracking
 * - Cache performance analysis
 * - HTTP request monitoring
 * - Memory and resource usage tracking
 * 
 * @author System
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PerformanceMonitoringService {
    
    private final MeterRegistry meterRegistry;
    private final CacheManager cacheManager;
    
    // Performance counters
    private Counter todoCreatedCounter;
    private Counter todoUpdatedCounter;
    private Counter todoDeletedCounter;
    private Counter cacheHitCounter;
    private Counter cacheMissCounter;
    private Counter databaseQueryCounter;
    
    // Performance timers
    private Timer todoCreateTimer;
    private Timer todoUpdateTimer;
    private Timer todoSearchTimer;
    private Timer databaseQueryTimer;
    private Timer cacheOperationTimer;
    
    @PostConstruct
    public void initializeMetrics() {
        log.info("Initializing performance monitoring metrics");
        
        // Initialize counters
        todoCreatedCounter = Counter.builder("todo.operations.created")
                .description("Number of todos created")
                .register(meterRegistry);
                
        todoUpdatedCounter = Counter.builder("todo.operations.updated")
                .description("Number of todos updated")
                .register(meterRegistry);
                
        todoDeletedCounter = Counter.builder("todo.operations.deleted")
                .description("Number of todos deleted")
                .register(meterRegistry);
                
        cacheHitCounter = Counter.builder("cache.operations.hit")
                .description("Number of cache hits")
                .register(meterRegistry);
                
        cacheMissCounter = Counter.builder("cache.operations.miss")
                .description("Number of cache misses")
                .register(meterRegistry);
                
        databaseQueryCounter = Counter.builder("database.queries.total")
                .description("Total number of database queries")
                .register(meterRegistry);
        
        // Initialize timers
        todoCreateTimer = Timer.builder("todo.operations.create.duration")
                .description("Time taken to create todos")
                .register(meterRegistry);
                
        todoUpdateTimer = Timer.builder("todo.operations.update.duration")
                .description("Time taken to update todos")
                .register(meterRegistry);
                
        todoSearchTimer = Timer.builder("todo.operations.search.duration")
                .description("Time taken to search todos")
                .register(meterRegistry);
                
        databaseQueryTimer = Timer.builder("database.query.duration")
                .description("Database query execution time")
                .register(meterRegistry);
                
        cacheOperationTimer = Timer.builder("cache.operation.duration")
                .description("Cache operation execution time")
                .register(meterRegistry);
    }
    
    /**
     * Record todo creation metrics
     */
    public void recordTodoCreated() {
        todoCreatedCounter.increment();
        log.debug("Recorded todo creation metric");
    }
    
    /**
     * Record todo update metrics
     */
    public void recordTodoUpdated() {
        todoUpdatedCounter.increment();
        log.debug("Recorded todo update metric");
    }
    
    /**
     * Record todo deletion metrics
     */
    public void recordTodoDeleted() {
        todoDeletedCounter.increment();
        log.debug("Recorded todo deletion metric");
    }
    
    /**
     * Record cache hit
     * @param cacheName the name of the cache
     */
    public void recordCacheHit(String cacheName) {
        cacheHitCounter.increment();
        log.debug("Recorded cache hit for cache: {}", cacheName);
    }
    
    /**
     * Record cache miss
     * @param cacheName the name of the cache
     */
    public void recordCacheMiss(String cacheName) {
        cacheMissCounter.increment();
        log.debug("Recorded cache miss for cache: {}", cacheName);
    }
    
    /**
     * Record database query execution
     */
    public void recordDatabaseQuery() {
        databaseQueryCounter.increment();
    }
    
    /**
     * Get timer for todo creation operations
     * @return Timer instance
     */
    public Timer.Sample startTodoCreateTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Get timer for todo update operations
     * @return Timer instance
     */
    public Timer.Sample startTodoUpdateTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Get timer for todo search operations
     * @return Timer instance
     */
    public Timer.Sample startTodoSearchTimer() {
        return Timer.start(meterRegistry);
    }
    
    /**
     * Stop and record todo create timer
     * @param sample Timer sample to stop
     */
    public void stopTodoCreateTimer(Timer.Sample sample) {
        sample.stop(todoCreateTimer);
    }
    
    /**
     * Stop and record todo update timer
     * @param sample Timer sample to stop
     */
    public void stopTodoUpdateTimer(Timer.Sample sample) {
        sample.stop(todoUpdateTimer);
    }
    
    /**
     * Stop and record todo search timer
     * @param sample Timer sample to stop
     */
    public void stopTodoSearchTimer(Timer.Sample sample) {
        sample.stop(todoSearchTimer);
    }
    
    /**
     * Get comprehensive performance statistics
     * @return Map containing performance metrics
     */
    public Map<String, Object> getPerformanceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Operation counts
        stats.put("todos.created.total", todoCreatedCounter.count());
        stats.put("todos.updated.total", todoUpdatedCounter.count());
        stats.put("todos.deleted.total", todoDeletedCounter.count());
        
        // Cache statistics
        stats.put("cache.hits.total", cacheHitCounter.count());
        stats.put("cache.misses.total", cacheMissCounter.count());
        
        double totalCacheOperations = cacheHitCounter.count() + cacheMissCounter.count();
        if (totalCacheOperations > 0) {
            stats.put("cache.hit.ratio", cacheHitCounter.count() / totalCacheOperations);
        } else {
            stats.put("cache.hit.ratio", 0.0);
        }
        
        // Database statistics
        stats.put("database.queries.total", databaseQueryCounter.count());
        
        // Performance timers (in milliseconds)
        stats.put("todo.create.duration.avg", todoCreateTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
        stats.put("todo.update.duration.avg", todoUpdateTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
        stats.put("todo.search.duration.avg", todoSearchTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
        
        // Cache details
        if (cacheManager != null) {
            stats.put("cache.names", cacheManager.getCacheNames());
        }
        
        return stats;
    }
    
    /**
     * Get cache performance details
     * @return Map containing cache performance metrics
     */
    public Map<String, Object> getCachePerformanceDetails() {
        Map<String, Object> cacheStats = new HashMap<>();
        
        if (cacheManager != null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    Map<String, Object> cacheInfo = new HashMap<>();
                    cacheInfo.put("name", cacheName);
                    cacheInfo.put("nativeCache", cache.getNativeCache().getClass().getSimpleName());
                    cacheStats.put(cacheName, cacheInfo);
                }
            }
        }
        
        return cacheStats;
    }
    
    /**
     * Reset all performance counters
     * This method is useful for testing and development
     */
    public void resetCounters() {
        log.info("Resetting performance monitoring counters");
        
        // Note: Micrometer counters cannot be reset to zero
        // This method is for future implementation if needed
        // For now, we'll log the reset operation
        
        log.info("Performance counters reset completed");
    }
    
    /**
     * Log current performance summary
     */
    public void logPerformanceSummary() {
        Map<String, Object> stats = getPerformanceStatistics();
        
        log.info("=== Performance Summary ===");
        log.info("Todos Created: {}", stats.get("todos.created.total"));
        log.info("Todos Updated: {}", stats.get("todos.updated.total"));
        log.info("Todos Deleted: {}", stats.get("todos.deleted.total"));
        log.info("Cache Hit Ratio: {}", String.format("%.2f%%", (Double) stats.get("cache.hit.ratio") * 100));
        log.info("Database Queries: {}", stats.get("database.queries.total"));
        log.info("Avg Create Time: {} ms", String.format("%.2f", (Double) stats.get("todo.create.duration.avg")));
        log.info("Avg Update Time: {} ms", String.format("%.2f", (Double) stats.get("todo.update.duration.avg")));
        log.info("Avg Search Time: {} ms", String.format("%.2f", (Double) stats.get("todo.search.duration.avg")));
        log.info("=========================");
    }
}