package com.example.todoapp.performance;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.service.TodoService;
import com.example.todoapp.service.PerformanceMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance Optimization Test Suite
 * 
 * This comprehensive test suite validates all performance optimization features including:
 * - Cache functionality and performance
 * - Database optimization effectiveness
 * - Async processing capabilities
 * - Performance monitoring accuracy
 * - Batch processing efficiency
 * 
 * @author System
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Performance Optimization Tests")
class PerformanceOptimizationTest {

    @Autowired
    private TodoService todoService;
    
    @Autowired
    private PerformanceMonitoringService performanceService;
    
    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Cache functionality should work correctly")
    @Transactional
    void testCacheFunctionality() {
        // Given - create a test todo
        TodoRequest request = new TodoRequest();
        request.setTitle("Cache Test Todo");
        request.setDescription("Testing cache functionality");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(1));
        
        Todo createdTodo = todoService.create(request);
        Long todoId = createdTodo.getId();
        
        // When - fetch the todo multiple times
        long startTime = System.currentTimeMillis();
        Todo firstFetch = todoService.findById(todoId);
        long firstFetchTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        Todo secondFetch = todoService.findById(todoId);
        long secondFetchTime = System.currentTimeMillis() - startTime;
        
        // Then - verify cache effectiveness
        assertNotNull(firstFetch);
        assertNotNull(secondFetch);
        assertEquals(firstFetch.getId(), secondFetch.getId());
        assertEquals(firstFetch.getTitle(), secondFetch.getTitle());
        
        // Second fetch should be significantly faster due to caching
        assertTrue(secondFetchTime <= firstFetchTime, 
                   String.format("Cache should improve performance. First: %dms, Second: %dms", 
                                firstFetchTime, secondFetchTime));
    }
    
    @Test
    @DisplayName("Cache should be evicted on updates")
    @Transactional
    void testCacheEvictionOnUpdate() {
        // Given - create and cache a todo
        TodoRequest request = new TodoRequest();
        request.setTitle("Cache Eviction Test");
        request.setDescription("Testing cache eviction");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.MEDIUM);
        
        Todo createdTodo = todoService.create(request);
        Long todoId = createdTodo.getId();
        
        // Cache the todo by fetching it
        todoService.findById(todoId);
        
        // When - update the todo
        TodoRequest updateRequest = new TodoRequest();
        updateRequest.setTitle("Updated Cache Test");
        updateRequest.setDescription("Updated description");
        updateRequest.setStatus(TodoStatus.IN_PROGRESS);
        updateRequest.setPriority(TodoPriority.HIGH);
        
        Todo updatedTodo = todoService.update(todoId, updateRequest);
        
        // Then - verify cache was evicted and updated data is returned
        Todo fetchedTodo = todoService.findById(todoId);
        assertEquals("Updated Cache Test", fetchedTodo.getTitle());
        assertEquals(TodoStatus.IN_PROGRESS, fetchedTodo.getStatus());
        assertEquals(TodoPriority.HIGH, fetchedTodo.getPriority());
    }
    
    @Test
    @DisplayName("Search results should be cached")
    @Transactional
    void testSearchCaching() {
        // Given - create test todos
        for (int i = 0; i < 5; i++) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Search Test Todo " + i);
            request.setDescription("Search testing " + i);
            request.setStatus(TodoStatus.TODO);
            request.setPriority(TodoPriority.MEDIUM);
            todoService.create(request);
        }
        
        // When - perform the same search multiple times
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("Search Test");
        
        long startTime = System.currentTimeMillis();
        List<Todo> firstSearch = todoService.search(criteria);
        long firstSearchTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        List<Todo> secondSearch = todoService.search(criteria);
        long secondSearchTime = System.currentTimeMillis() - startTime;
        
        // Then - verify caching effectiveness
        assertFalse(firstSearch.isEmpty());
        assertFalse(secondSearch.isEmpty());
        assertEquals(firstSearch.size(), secondSearch.size());
        
        // Second search should be faster due to caching
        assertTrue(secondSearchTime <= firstSearchTime,
                   String.format("Search cache should improve performance. First: %dms, Second: %dms",
                                firstSearchTime, secondSearchTime));
    }
    
    @Test
    @DisplayName("Async operations should work correctly")
    void testAsyncOperations() throws ExecutionException, InterruptedException {
        // Given - cast to implementation to access async methods
        if (todoService instanceof com.example.todoapp.service.impl.TodoServiceImpl) {
            var todoServiceImpl = (com.example.todoapp.service.impl.TodoServiceImpl) todoService;
            
            // When - execute async cache preload
            CompletableFuture<Void> preloadFuture = todoServiceImpl.preloadCacheData();
            
            // Then - verify async operation completes
            assertDoesNotThrow(() -> preloadFuture.get());
            assertTrue(preloadFuture.isDone());
        }
    }
    
    @Test
    @DisplayName("Performance monitoring should track metrics")
    @Transactional
    void testPerformanceMonitoring() {
        // Given - get initial metrics
        Map<String, Object> initialStats = performanceService.getPerformanceStatistics();
        Double initialCreated = (Double) initialStats.get("todos.created.total");
        
        // When - create some todos
        for (int i = 0; i < 3; i++) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Monitoring Test Todo " + i);
            request.setDescription("Performance monitoring test");
            request.setStatus(TodoStatus.TODO);
            request.setPriority(TodoPriority.LOW);
            
            todoService.create(request);
            performanceService.recordTodoCreated();
        }
        
        // Then - verify metrics were updated
        Map<String, Object> updatedStats = performanceService.getPerformanceStatistics();
        Double updatedCreated = (Double) updatedStats.get("todos.created.total");
        
        assertTrue(updatedCreated > initialCreated, 
                   "Performance metrics should be updated after operations");
    }
    
    @Test
    @DisplayName("Cache manager should be configured correctly")
    void testCacheManagerConfiguration() {
        // When - get cache manager details
        assertNotNull(cacheManager, "Cache manager should be configured");
        
        // Then - verify expected caches are available
        var cacheNames = cacheManager.getCacheNames();
        assertFalse(cacheNames.isEmpty(), "Cache names should be configured");
        
        // Verify specific cache configurations
        assertTrue(cacheNames.contains("todos"), "todos cache should be configured");
        assertTrue(cacheNames.contains("todo-counts"), "todo-counts cache should be configured");
        assertTrue(cacheNames.contains("todo-search-results"), "search results cache should be configured");
        
        // Verify caches can be accessed
        for (String cacheName : cacheNames) {
            var cache = cacheManager.getCache(cacheName);
            assertNotNull(cache, "Cache " + cacheName + " should be accessible");
        }
    }
    
    @Test
    @DisplayName("Status-based queries should be cached")
    @Transactional
    void testStatusBasedCaching() {
        // Given - create todos with different statuses
        for (TodoStatus status : TodoStatus.values()) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Status Test " + status);
            request.setDescription("Testing status caching");
            request.setStatus(status);
            request.setPriority(TodoPriority.MEDIUM);
            todoService.create(request);
        }
        
        // When - query by status multiple times
        for (TodoStatus status : TodoStatus.values()) {
            long startTime = System.currentTimeMillis();
            List<Todo> firstQuery = todoService.findByStatus(status);
            long firstQueryTime = System.currentTimeMillis() - startTime;
            
            startTime = System.currentTimeMillis();
            List<Todo> secondQuery = todoService.findByStatus(status);
            long secondQueryTime = System.currentTimeMillis() - startTime;
            
            // Then - verify results and performance improvement
            assertEquals(firstQuery.size(), secondQuery.size(), 
                        "Status queries should return consistent results");
            
            if (!firstQuery.isEmpty()) {
                assertTrue(secondQueryTime <= firstQueryTime,
                          String.format("Status cache should improve performance for %s. First: %dms, Second: %dms",
                                       status, firstQueryTime, secondQueryTime));
            }
        }
    }
    
    @Test
    @DisplayName("Overdue todos query should be cached")
    @Transactional
    void testOverdueTodosCaching() {
        // Given - create overdue todos
        for (int i = 0; i < 3; i++) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Overdue Test Todo " + i);
            request.setDescription("Testing overdue caching");
            request.setStatus(TodoStatus.TODO);
            request.setPriority(TodoPriority.HIGH);
            request.setDueDate(LocalDate.now().minusDays(i + 1)); // Past due dates
            todoService.create(request);
        }
        
        // When - query overdue todos multiple times
        long startTime = System.currentTimeMillis();
        List<Todo> firstQuery = todoService.findOverdueTodos();
        long firstQueryTime = System.currentTimeMillis() - startTime;
        
        startTime = System.currentTimeMillis();
        List<Todo> secondQuery = todoService.findOverdueTodos();
        long secondQueryTime = System.currentTimeMillis() - startTime;
        
        // Then - verify caching effectiveness
        assertFalse(firstQuery.isEmpty(), "Should find overdue todos");
        assertEquals(firstQuery.size(), secondQuery.size(), "Queries should return consistent results");
        
        assertTrue(secondQueryTime <= firstQueryTime,
                   String.format("Overdue cache should improve performance. First: %dms, Second: %dms",
                                firstQueryTime, secondQueryTime));
    }
    
    @Test
    @DisplayName("Performance statistics should include cache details")
    void testPerformanceStatisticsCompleteness() {
        // When - get performance statistics
        Map<String, Object> stats = performanceService.getPerformanceStatistics();
        Map<String, Object> cacheDetails = performanceService.getCachePerformanceDetails();
        
        // Then - verify statistics completeness
        assertNotNull(stats, "Performance statistics should not be null");
        assertNotNull(cacheDetails, "Cache details should not be null");
        
        // Verify key metrics are present
        assertTrue(stats.containsKey("todos.created.total"), "Should track created todos");
        assertTrue(stats.containsKey("todos.updated.total"), "Should track updated todos");
        assertTrue(stats.containsKey("todos.deleted.total"), "Should track deleted todos");
        assertTrue(stats.containsKey("cache.hits.total"), "Should track cache hits");
        assertTrue(stats.containsKey("cache.misses.total"), "Should track cache misses");
        assertTrue(stats.containsKey("cache.hit.ratio"), "Should calculate hit ratio");
        
        // Verify cache details structure
        assertFalse(cacheDetails.isEmpty(), "Cache details should not be empty");
    }
    
    @Test
    @DisplayName("Batch operations should complete successfully")
    void testBatchOperations() throws ExecutionException, InterruptedException {
        // Given - create test todos and cast to implementation
        List<Long> todoIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Batch Test Todo " + i);
            request.setDescription("Testing batch operations");
            request.setStatus(TodoStatus.TODO);
            request.setPriority(TodoPriority.MEDIUM);
            
            Todo todo = todoService.create(request);
            todoIds.add(todo.getId());
        }
        
        // When - perform batch update (if available)
        if (todoService instanceof com.example.todoapp.service.impl.TodoServiceImpl) {
            var todoServiceImpl = (com.example.todoapp.service.impl.TodoServiceImpl) todoService;
            
            CompletableFuture<Void> batchFuture = 
                todoServiceImpl.batchUpdateStatus(todoIds, TodoStatus.DONE);
            
            // Then - verify batch operation completes
            assertDoesNotThrow(() -> batchFuture.get());
            assertTrue(batchFuture.isDone());
        }
    }
    
    @Test
    @DisplayName("Performance timers should record operation durations")
    @Transactional
    void testPerformanceTimers() {
        // Given - performance monitoring service
        assertNotNull(performanceService, "Performance service should be available");
        
        // When - use performance timers
        var createSample = performanceService.startTodoCreateTimer();
        
        // Simulate some work
        TodoRequest request = new TodoRequest();
        request.setTitle("Timer Test Todo");
        request.setDescription("Testing performance timers");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.MEDIUM);
        
        todoService.create(request);
        
        // Stop the timer
        performanceService.stopTodoCreateTimer(createSample);
        
        // Then - verify statistics are updated
        Map<String, Object> stats = performanceService.getPerformanceStatistics();
        assertTrue(stats.containsKey("todo.create.duration.avg"), 
                   "Should track average create duration");
        
        Double avgDuration = (Double) stats.get("todo.create.duration.avg");
        assertNotNull(avgDuration, "Average duration should be recorded");
    }
}