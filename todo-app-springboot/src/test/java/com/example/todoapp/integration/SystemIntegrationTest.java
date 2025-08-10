package com.example.todoapp.integration;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.service.TodoService;
import com.example.todoapp.service.LoggingService;
import com.example.todoapp.service.PerformanceMonitoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * システム全体の統合テスト
 * 
 * 実際のHTTPサーバーを起動して、全てのコンポーネント間の統合をテストします。
 * 
 * @author System
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("System Integration Tests")
class SystemIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoService todoService;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        todoRepository.deleteAll();
    }

    @Test
    @DisplayName("Full System Startup and Health Checks")
    void testSystemStartupAndHealthChecks() {
        // Test basic application health
        ResponseEntity<Map> healthResponse = restTemplate.getForEntity(
            baseUrl + "/actuator/health", Map.class);
        assertEquals(HttpStatus.OK, healthResponse.getStatusCode());
        
        Map<String, Object> health = healthResponse.getBody();
        assertNotNull(health);
        assertEquals("UP", health.get("status"));

        // Test production health indicator
        ResponseEntity<Map> prodHealthResponse = restTemplate.getForEntity(
            baseUrl + "/actuator/health/production", Map.class);
        assertEquals(HttpStatus.OK, prodHealthResponse.getStatusCode());

        // Test info endpoint
        ResponseEntity<Map> infoResponse = restTemplate.getForEntity(
            baseUrl + "/actuator/info", Map.class);
        assertEquals(HttpStatus.OK, infoResponse.getStatusCode());
        
        // Test metrics endpoint
        ResponseEntity<Map> metricsResponse = restTemplate.getForEntity(
            baseUrl + "/actuator/metrics", Map.class);
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    }

    @Test
    @DisplayName("Complete REST API Integration")
    @Transactional
    void testCompleteRestApiIntegration() {
        // Create Todo
        TodoRequest createRequest = new TodoRequest();
        createRequest.setTitle("System Integration Test");
        createRequest.setDescription("Complete system integration test");
        createRequest.setStatus(TodoStatus.TODO);
        createRequest.setPriority(TodoPriority.HIGH);
        createRequest.setDueDate(LocalDate.now().plusDays(5));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TodoRequest> entity = new HttpEntity<>(createRequest, headers);

        ResponseEntity<Todo> createResponse = restTemplate.postForEntity(
            baseUrl + "/api/todos", entity, Todo.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        
        Todo createdTodo = createResponse.getBody();
        assertNotNull(createdTodo);
        assertNotNull(createdTodo.getId());
        assertEquals("System Integration Test", createdTodo.getTitle());

        // Read Todo
        ResponseEntity<Todo> readResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/" + createdTodo.getId(), Todo.class);
        assertEquals(HttpStatus.OK, readResponse.getStatusCode());
        assertEquals(createdTodo.getId(), readResponse.getBody().getId());

        // Update Todo
        createRequest.setTitle("Updated System Integration Test");
        createRequest.setStatus(TodoStatus.IN_PROGRESS);
        HttpEntity<TodoRequest> updateEntity = new HttpEntity<>(createRequest, headers);
        
        ResponseEntity<Todo> updateResponse = restTemplate.exchange(
            baseUrl + "/api/todos/" + createdTodo.getId(),
            HttpMethod.PUT, updateEntity, Todo.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals("Updated System Integration Test", updateResponse.getBody().getTitle());

        // List Todos
        ResponseEntity<Map> listResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos", Map.class);
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        
        Map<String, Object> pageResult = listResponse.getBody();
        assertNotNull(pageResult);
        assertTrue(pageResult.containsKey("content"));

        // Search Todos
        ResponseEntity<Todo[]> searchResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/search?keyword=System", Todo[].class);
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertTrue(searchResponse.getBody().length > 0);

        // Delete Todo
        restTemplate.delete(baseUrl + "/api/todos/" + createdTodo.getId());
        
        // Verify deletion
        ResponseEntity<String> deletedResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/" + createdTodo.getId(), String.class);
        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
    }

    @Test
    @DisplayName("Web Interface Integration")
    void testWebInterfaceIntegration() {
        // Test main todo list page
        ResponseEntity<String> listPageResponse = restTemplate.getForEntity(
            baseUrl + "/todos", String.class);
        assertEquals(HttpStatus.OK, listPageResponse.getStatusCode());
        assertTrue(listPageResponse.getBody().contains("Todo"));

        // Test create page
        ResponseEntity<String> createPageResponse = restTemplate.getForEntity(
            baseUrl + "/todos/new", String.class);
        assertEquals(HttpStatus.OK, createPageResponse.getStatusCode());

        // Test search page
        ResponseEntity<String> searchPageResponse = restTemplate.getForEntity(
            baseUrl + "/todos/search", String.class);
        assertEquals(HttpStatus.OK, searchPageResponse.getStatusCode());

        // Create a todo for detail/edit page tests
        Todo testTodo = new Todo();
        testTodo.setTitle("Web Test Todo");
        testTodo.setDescription("For web interface testing");
        testTodo.setStatus(TodoStatus.TODO);
        testTodo.setPriority(TodoPriority.MEDIUM);
        Todo savedTodo = todoRepository.save(testTodo);

        // Test detail page
        ResponseEntity<String> detailPageResponse = restTemplate.getForEntity(
            baseUrl + "/todos/" + savedTodo.getId(), String.class);
        assertEquals(HttpStatus.OK, detailPageResponse.getStatusCode());
        assertTrue(detailPageResponse.getBody().contains("Web Test Todo"));

        // Test edit page
        ResponseEntity<String> editPageResponse = restTemplate.getForEntity(
            baseUrl + "/todos/" + savedTodo.getId() + "/edit", String.class);
        assertEquals(HttpStatus.OK, editPageResponse.getStatusCode());
    }

    @Test
    @DisplayName("Logging System Integration")
    void testLoggingSystemIntegration() {
        // Test log level retrieval
        ResponseEntity<Map> logLevelsResponse = restTemplate.getForEntity(
            baseUrl + "/admin/logging/levels", Map.class);
        assertEquals(HttpStatus.OK, logLevelsResponse.getStatusCode());

        // Test log information
        ResponseEntity<Map> logInfoResponse = restTemplate.getForEntity(
            baseUrl + "/admin/logging/info", Map.class);
        assertEquals(HttpStatus.OK, logInfoResponse.getStatusCode());

        // Verify logging service functionality
        assertDoesNotThrow(() -> {
            loggingService.logBusinessOperation("integration-test", "Test log message from integration test");
            loggingService.logSecurity("test-security", "Test error message from integration test");
        });
    }

    @Test
    @DisplayName("Performance Monitoring Integration")
    void testPerformanceMonitoringIntegration() {
        // Test performance metrics endpoint
        ResponseEntity<Map> metricsResponse = restTemplate.getForEntity(
            baseUrl + "/dev/performance/metrics", Map.class);
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        
        Map<String, Object> metrics = metricsResponse.getBody();
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("operationCounts"));

        // Test performance report
        ResponseEntity<Map> reportResponse = restTemplate.getForEntity(
            baseUrl + "/dev/performance/report", Map.class);
        assertEquals(HttpStatus.OK, reportResponse.getStatusCode());

        // Verify performance monitoring service
        assertDoesNotThrow(() -> {
            performanceMonitoringService.recordTodoCreated();
            performanceMonitoringService.recordCacheHit("test-cache");
        });
    }

    @Test
    @DisplayName("Concurrent Operations Integration")
    void testConcurrentOperationsIntegration() throws Exception {
        int concurrentRequests = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // Create multiple todos concurrently
        CompletableFuture<?>[] futures = new CompletableFuture[concurrentRequests];
        
        for (int i = 0; i < concurrentRequests; i++) {
            final int index = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                try {
                    TodoRequest request = new TodoRequest();
                    request.setTitle("Concurrent Test " + index);
                    request.setDescription("Concurrent integration test " + index);
                    request.setStatus(TodoStatus.TODO);
                    request.setPriority(TodoPriority.MEDIUM);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<TodoRequest> entity = new HttpEntity<>(request, headers);

                    ResponseEntity<Todo> response = restTemplate.postForEntity(
                        baseUrl + "/api/todos", entity, Todo.class);
                    
                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Concurrent request failed: " + e.getMessage());
                }
            });
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

        // Verify results
        assertTrue(successCount.get() > 0, "At least some concurrent requests should succeed");
        System.out.println("Concurrent test results: " + successCount.get() + " successes, " + errorCount.get() + " errors");

        // Verify database consistency
        long dbCount = todoRepository.count();
        assertEquals(successCount.get(), dbCount, "Database count should match successful creations");
    }

    @Test
    @DisplayName("Error Handling Integration")
    void testErrorHandlingIntegration() {
        // Test 404 error handling
        ResponseEntity<String> notFoundResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/99999", String.class);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());

        // Test validation error handling
        TodoRequest invalidRequest = new TodoRequest();
        // Missing required title
        invalidRequest.setDescription("Missing title");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TodoRequest> entity = new HttpEntity<>(invalidRequest, headers);

        ResponseEntity<String> validationErrorResponse = restTemplate.postForEntity(
            baseUrl + "/api/todos", entity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, validationErrorResponse.getStatusCode());

        // Test method not allowed error
        ResponseEntity<String> methodNotAllowedResponse = restTemplate.exchange(
            baseUrl + "/api/todos", HttpMethod.PATCH, entity, String.class);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, methodNotAllowedResponse.getStatusCode());
    }

    @Test
    @DisplayName("Database Integration and Persistence")
    @Transactional
    void testDatabaseIntegrationAndPersistence() {
        // Create todo via service layer
        TodoRequest request = new TodoRequest();
        request.setTitle("Database Integration Test");
        request.setDescription("Testing database persistence");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(7));

        Todo createdTodo = todoService.create(request);
        assertNotNull(createdTodo.getId());

        // Verify persistence via repository
        assertTrue(todoRepository.existsById(createdTodo.getId()));
        Todo repositoryTodo = todoRepository.findById(createdTodo.getId()).orElse(null);
        assertNotNull(repositoryTodo);
        assertEquals("Database Integration Test", repositoryTodo.getTitle());

        // Verify via REST API
        ResponseEntity<Todo> apiResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/" + createdTodo.getId(), Todo.class);
        assertEquals(HttpStatus.OK, apiResponse.getStatusCode());
        assertEquals(createdTodo.getId(), apiResponse.getBody().getId());

        // Test database transaction consistency
        request.setTitle("Updated Database Test");
        Todo updatedTodo = todoService.update(createdTodo.getId(), request);
        
        // Verify update across all layers
        Todo repoUpdatedTodo = todoRepository.findById(createdTodo.getId()).orElse(null);
        assertNotNull(repoUpdatedTodo);
        assertEquals("Updated Database Test", repoUpdatedTodo.getTitle());
        assertTrue(repoUpdatedTodo.getUpdatedAt().isAfter(repoUpdatedTodo.getCreatedAt()));
    }

    @Test
    @DisplayName("Caching and Performance Integration")
    @Transactional
    void testCachingAndPerformanceIntegration() {
        // Create test data
        TodoRequest request = new TodoRequest();
        request.setTitle("Cache Test Todo");
        request.setDescription("Testing caching integration");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.MEDIUM);

        Todo createdTodo = todoService.create(request);

        // First request (cache miss)
        long startTime = System.currentTimeMillis();
        Todo firstRead = todoService.findById(createdTodo.getId());
        long firstRequestTime = System.currentTimeMillis() - startTime;

        // Second request (should be cached)
        startTime = System.currentTimeMillis();
        Todo secondRead = todoService.findById(createdTodo.getId());
        long secondRequestTime = System.currentTimeMillis() - startTime;

        // Verify both reads return the same data
        assertEquals(firstRead.getId(), secondRead.getId());
        assertEquals(firstRead.getTitle(), secondRead.getTitle());

        // Performance metrics should be recorded
        Map<String, Object> metrics = performanceMonitoringService.getPerformanceStatistics();
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("todos.created.total"));

        System.out.println("Cache test - First request: " + firstRequestTime + "ms, Second request: " + secondRequestTime + "ms");
    }

    @Test
    @DisplayName("Full Application Lifecycle Test")
    @Transactional
    void testFullApplicationLifecycle() throws Exception {
        // Phase 1: Application startup verification
        assertNotNull(todoService);
        assertNotNull(todoRepository);
        assertEquals(0, todoRepository.count());

        // Phase 2: Data creation workflow
        TodoRequest request = new TodoRequest();
        request.setTitle("Lifecycle Test Todo");
        request.setDescription("Full application lifecycle test");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(10));

        // Create via REST API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TodoRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Todo> createResponse = restTemplate.postForEntity(
            baseUrl + "/api/todos", entity, Todo.class);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Todo createdTodo = createResponse.getBody();

        // Phase 3: Data processing workflow
        // Update status through different layers
        TodoRequest statusUpdateRequest = new TodoRequest();
        statusUpdateRequest.setTitle(createdTodo.getTitle());
        statusUpdateRequest.setDescription(createdTodo.getDescription());
        statusUpdateRequest.setStatus(TodoStatus.IN_PROGRESS);
        statusUpdateRequest.setPriority(createdTodo.getPriority());
        statusUpdateRequest.setDueDate(createdTodo.getDueDate());
        Todo serviceUpdated = todoService.update(createdTodo.getId(), statusUpdateRequest);
        assertEquals(TodoStatus.IN_PROGRESS, serviceUpdated.getStatus());

        // Complete via API
        request.setStatus(TodoStatus.DONE);
        HttpEntity<TodoRequest> updateEntity = new HttpEntity<>(request, headers);
        ResponseEntity<Todo> updateResponse = restTemplate.exchange(
            baseUrl + "/api/todos/" + createdTodo.getId(),
            HttpMethod.PUT, updateEntity, Todo.class);
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(TodoStatus.DONE, updateResponse.getBody().getStatus());

        // Phase 4: Search and reporting
        ResponseEntity<Todo[]> searchResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/search?status=DONE", Todo[].class);
        assertEquals(HttpStatus.OK, searchResponse.getStatusCode());
        assertTrue(searchResponse.getBody().length > 0);

        // Phase 5: Performance monitoring verification
        Map<String, Object> performanceReport = performanceMonitoringService.getPerformanceStatistics();
        assertNotNull(performanceReport);
        assertTrue(performanceReport.containsKey("todos.created.total"));

        // Phase 6: Cleanup and verification
        restTemplate.delete(baseUrl + "/api/todos/" + createdTodo.getId());
        
        ResponseEntity<String> deletedResponse = restTemplate.getForEntity(
            baseUrl + "/api/todos/" + createdTodo.getId(), String.class);
        assertEquals(HttpStatus.NOT_FOUND, deletedResponse.getStatusCode());
        
        assertEquals(0, todoRepository.count());
    }
}