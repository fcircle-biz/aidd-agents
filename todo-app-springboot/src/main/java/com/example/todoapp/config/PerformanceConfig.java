package com.example.todoapp.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Performance optimization configuration class
 * 
 * This class provides comprehensive performance tuning configurations including:
 * - Caching strategy using Caffeine cache
 * - Asynchronous processing configuration
 * - Thread pool optimization for better performance
 * - Cache management and eviction policies
 * 
 * @author System
 */
@Configuration
@EnableCaching
@EnableAsync
public class PerformanceConfig {

    /**
     * Primary cache manager using Caffeine cache for high performance
     * 
     * Caffeine is chosen over other cache implementations for:
     * - Better performance characteristics
     * - Lower memory footprint
     * - Advanced eviction policies
     * - Thread-safe operations
     * 
     * @return configured CacheManager instance
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure default cache settings
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .recordStats()
        );
        
        // Define cache names for different data types
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "todos",
                "todo-counts", 
                "todo-search-results",
                "todo-statistics",
                "overdue-todos",
                "status-counts",
                "priority-counts"
        ));
        
        return cacheManager;
    }

    /**
     * Production cache manager with enhanced settings for production workloads
     * 
     * @return production-optimized CacheManager
     */
    @Bean
    @Profile("prod")
    @Primary
    public CacheManager productionCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Production cache settings with larger capacity and longer TTL
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .recordStats()
        );
        
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "todos",
                "todo-counts", 
                "todo-search-results",
                "todo-statistics",
                "overdue-todos",
                "status-counts",
                "priority-counts"
        ));
        
        return cacheManager;
    }

    /**
     * Async task executor for non-blocking operations
     * 
     * Optimized thread pool for handling asynchronous operations:
     * - Database operations
     * - File processing
     * - Email notifications
     * - Background tasks
     * 
     * @return configured ThreadPoolTaskExecutor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - minimum threads to keep alive
        executor.setCorePoolSize(5);
        
        // Maximum pool size - maximum threads allowed
        executor.setMaxPoolSize(20);
        
        // Queue capacity - tasks to queue before creating new threads
        executor.setQueueCapacity(100);
        
        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("TodoApp-Async-");
        
        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);
        
        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);
        
        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        
        executor.initialize();
        return executor;
    }

    /**
     * Production async executor with enhanced settings
     * 
     * @return production-optimized ThreadPoolTaskExecutor
     */
    @Bean(name = "productionTaskExecutor")
    @Profile("prod")
    public Executor productionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Production settings with higher capacity
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("TodoApp-Prod-Async-");
        executor.setKeepAliveSeconds(300);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Development cache manager with debugging features
     * 
     * @return development-optimized CacheManager
     */
    @Bean
    @Profile("dev")
    @Primary
    public CacheManager developmentCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Development cache settings with shorter TTL for testing
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .recordStats()
                // Enable debug logging for cache operations
        );
        
        cacheManager.setCacheNames(java.util.Arrays.asList(
                "todos",
                "todo-counts", 
                "todo-search-results",
                "todo-statistics",
                "overdue-todos",
                "status-counts",
                "priority-counts"
        ));
        
        return cacheManager;
    }
}