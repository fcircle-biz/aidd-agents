package com.example.todoapp.integration;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 完全なエンドツーエンド統合テストスイート
 * 
 * このテストクラスは、アプリケーションの全ての層（Web、Service、Repository、Database）
 * を通したフロー全体をテストします。
 * 
 * @author System
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Complete End-to-End Integration Tests")
class CompleteEndToEndIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoService todoService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        todoRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Application Context Loads Successfully")
    void testApplicationContextLoads() {
        assertNotNull(webApplicationContext, "Web Application Context should load");
        assertNotNull(todoRepository, "TodoRepository should be injected");
        assertNotNull(todoService, "TodoService should be injected");
        assertNotNull(objectMapper, "ObjectMapper should be injected");
    }

    @Test
    @Order(2)
    @DisplayName("Complete Todo Lifecycle - REST API Flow")
    @Transactional
    void testCompleteRestApiLifecycle() throws Exception {
        // Phase 1: Create Todo via REST API
        TodoRequest createRequest = new TodoRequest();
        createRequest.setTitle("End-to-End Test Todo");
        createRequest.setDescription("Complete lifecycle test through REST API");
        createRequest.setStatus(TodoStatus.TODO);
        createRequest.setPriority(TodoPriority.HIGH);
        createRequest.setDueDate(LocalDate.now().plusDays(7));

        MvcResult createResult = mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("End-to-End Test Todo"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Todo createdTodo = objectMapper.readValue(createResponse, Todo.class);
        Long todoId = createdTodo.getId();

        // Phase 2: Verify creation through database
        assertTrue(todoRepository.existsById(todoId), "Todo should exist in database");
        Todo dbTodo = todoRepository.findById(todoId).orElse(null);
        assertNotNull(dbTodo, "Todo should be retrievable from database");
        assertEquals("End-to-End Test Todo", dbTodo.getTitle());
        assertNotNull(dbTodo.getCreatedAt(), "CreatedAt should be set");
        assertNotNull(dbTodo.getUpdatedAt(), "UpdatedAt should be set");

        // Phase 3: Read via REST API
        mockMvc.perform(get("/api/todos/{id}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value("End-to-End Test Todo"))
                .andExpect(jsonPath("$.status").value("TODO"));

        // Phase 4: Update via REST API
        TodoRequest updateRequest = new TodoRequest();
        updateRequest.setTitle("Updated E2E Todo");
        updateRequest.setDescription("Updated through complete flow");
        updateRequest.setStatus(TodoStatus.IN_PROGRESS);
        updateRequest.setPriority(TodoPriority.MEDIUM);
        updateRequest.setDueDate(LocalDate.now().plusDays(14));

        mockMvc.perform(put("/api/todos/{id}", todoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(todoId))
                .andExpect(jsonPath("$.title").value("Updated E2E Todo"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // Phase 5: Verify update through database
        Todo updatedDbTodo = todoRepository.findById(todoId).orElse(null);
        assertNotNull(updatedDbTodo);
        assertEquals("Updated E2E Todo", updatedDbTodo.getTitle());
        assertEquals(TodoStatus.IN_PROGRESS, updatedDbTodo.getStatus());
        assertEquals(TodoPriority.MEDIUM, updatedDbTodo.getPriority());
        assertTrue(updatedDbTodo.getUpdatedAt().isAfter(updatedDbTodo.getCreatedAt()));

        // Phase 6: Complete via Service Layer
        TodoRequest completionRequest = new TodoRequest();
        completionRequest.setTitle(updatedDbTodo.getTitle());
        completionRequest.setDescription(updatedDbTodo.getDescription());
        completionRequest.setStatus(TodoStatus.DONE);
        completionRequest.setPriority(updatedDbTodo.getPriority());
        completionRequest.setDueDate(updatedDbTodo.getDueDate());
        Todo completedTodo = todoService.update(todoId, completionRequest);
        assertEquals(TodoStatus.DONE, completedTodo.getStatus());

        // Phase 7: Verify completion via REST API
        mockMvc.perform(get("/api/todos/{id}", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        // Phase 8: Delete via REST API
        mockMvc.perform(delete("/api/todos/{id}", todoId))
                .andExpect(status().isNoContent());

        // Phase 9: Verify deletion
        mockMvc.perform(get("/api/todos/{id}", todoId))
                .andExpect(status().isNotFound());
        
        assertFalse(todoRepository.existsById(todoId), "Todo should be deleted from database");
    }

    @Test
    @Order(3)
    @DisplayName("Complex Search and Filter Operations")
    @Transactional
    void testComplexSearchAndFilterOperations() throws Exception {
        // Create diverse test data
        createTestTodosForSearching();

        // Wait a brief moment to ensure all data is persisted
        TimeUnit.MILLISECONDS.sleep(100);

        // Test 1: Keyword search
        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));

        // Test 2: Status-based search
        mockMvc.perform(get("/api/todos/search")
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Test 3: Combined search criteria
        mockMvc.perform(get("/api/todos/search")
                .param("keyword", "framework")
                .param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Test 4: Date range search (if implemented)
        mockMvc.perform(get("/api/todos/search")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Test 5: Pagination with search
        mockMvc.perform(get("/api/todos")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    @Order(4)
    @DisplayName("Bulk Operations and Performance")
    @Transactional
    void testBulkOperationsAndPerformance() throws Exception {
        // Create multiple todos for bulk operations
        int todoCount = 10;
        for (int i = 1; i <= todoCount; i++) {
            TodoRequest request = new TodoRequest();
            request.setTitle("Bulk Todo " + i);
            request.setDescription("Bulk operation test todo " + i);
            request.setStatus(i % 3 == 0 ? TodoStatus.DONE : (i % 2 == 0 ? TodoStatus.IN_PROGRESS : TodoStatus.TODO));
            request.setPriority(TodoPriority.values()[i % 3]);
            request.setDueDate(LocalDate.now().plusDays(i));

            mockMvc.perform(post("/api/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Verify all todos were created
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(todoCount));

        // Test database-level count
        long dbCount = todoRepository.count();
        assertEquals(todoCount, dbCount, "Database should contain all created todos");

        // Test performance of list operations
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/todos")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk());
        long endTime = System.currentTimeMillis();
        
        assertTrue(endTime - startTime < 5000, "List operation should complete within 5 seconds");
    }

    @Test
    @Order(5)
    @DisplayName("Error Handling and Edge Cases")
    void testErrorHandlingAndEdgeCases() throws Exception {
        // Test 1: Invalid TODO ID
        mockMvc.perform(get("/api/todos/99999"))
                .andExpect(status().isNotFound());

        // Test 2: Invalid data for creation
        TodoRequest invalidRequest = new TodoRequest();
        // Missing required title
        invalidRequest.setDescription("Missing title");

        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Test 3: Invalid JSON format
        mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        // Test 4: Invalid HTTP method
        mockMvc.perform(patch("/api/todos"))
                .andExpect(status().isMethodNotAllowed());

        // Test 5: Empty search parameters (should return all)
        mockMvc.perform(get("/api/todos/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(6)
    @DisplayName("Web Interface Integration")
    void testWebInterfaceIntegration() throws Exception {
        // Test main pages accessibility
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/list"));

        mockMvc.perform(get("/todos/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/create"));

        mockMvc.perform(get("/todos/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/search"));

        // Create a todo to test detail and edit pages
        Todo testTodo = new Todo();
        testTodo.setTitle("Web Interface Test");
        testTodo.setDescription("Testing web interface integration");
        testTodo.setStatus(TodoStatus.TODO);
        testTodo.setPriority(TodoPriority.MEDIUM);
        Todo savedTodo = todoRepository.save(testTodo);

        // Test detail page
        mockMvc.perform(get("/todos/{id}", savedTodo.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/detail"));

        // Test edit page
        mockMvc.perform(get("/todos/{id}/edit", savedTodo.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/edit"));
    }

    @Test
    @Order(7)
    @DisplayName("Service Layer Integration")
    @Transactional
    void testServiceLayerIntegration() {
        // Test service layer direct integration
        TodoRequest request = new TodoRequest();
        request.setTitle("Service Layer Test");
        request.setDescription("Testing service layer integration");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));

        // Create via service
        Todo createdTodo = todoService.create(request);
        assertNotNull(createdTodo.getId());
        assertEquals("Service Layer Test", createdTodo.getTitle());

        // Read via service
        Todo foundTodo = todoService.findById(createdTodo.getId());
        assertEquals(createdTodo.getId(), foundTodo.getId());
        assertEquals("Service Layer Test", foundTodo.getTitle());

        // Update via service
        request.setTitle("Updated Service Test");
        request.setStatus(TodoStatus.IN_PROGRESS);
        Todo updatedTodo = todoService.update(createdTodo.getId(), request);
        assertEquals("Updated Service Test", updatedTodo.getTitle());
        assertEquals(TodoStatus.IN_PROGRESS, updatedTodo.getStatus());

        // Search via service
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("Service");
        List<Todo> searchResults = todoService.search(criteria);
        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(t -> t.getTitle().contains("Service")));

        // Delete via service
        todoService.delete(createdTodo.getId());
        assertThrows(RuntimeException.class, () -> todoService.findById(createdTodo.getId()));
    }

    @Test
    @Order(8)
    @DisplayName("Data Consistency and Transaction Handling")
    @Transactional
    void testDataConsistencyAndTransactions() throws Exception {
        // Test data consistency across layers
        TodoRequest request = new TodoRequest();
        request.setTitle("Consistency Test");
        request.setDescription("Testing data consistency");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.MEDIUM);

        // Create via REST API
        MvcResult result = mockMvc.perform(post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Todo createdTodo = objectMapper.readValue(result.getResponse().getContentAsString(), Todo.class);

        // Verify via Service Layer
        Todo serviceTodo = todoService.findById(createdTodo.getId());
        assertEquals(createdTodo.getTitle(), serviceTodo.getTitle());
        assertEquals(createdTodo.getStatus(), serviceTodo.getStatus());

        // Verify via Repository Layer
        Todo repoTodo = todoRepository.findById(createdTodo.getId()).orElse(null);
        assertNotNull(repoTodo);
        assertEquals(createdTodo.getTitle(), repoTodo.getTitle());
        assertEquals(createdTodo.getStatus(), repoTodo.getStatus());

        // Test timestamp consistency
        assertNotNull(repoTodo.getCreatedAt());
        assertNotNull(repoTodo.getUpdatedAt());
        assertEquals(repoTodo.getCreatedAt(), repoTodo.getUpdatedAt());

        // Update and verify timestamp changes
        Thread.sleep(10); // Ensure time difference
        request.setTitle("Updated Consistency Test");
        mockMvc.perform(put("/api/todos/{id}", createdTodo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Todo updatedRepoTodo = todoRepository.findById(createdTodo.getId()).orElse(null);
        assertNotNull(updatedRepoTodo);
        assertTrue(updatedRepoTodo.getUpdatedAt().isAfter(updatedRepoTodo.getCreatedAt()));
    }

    /**
     * テスト用のTodoデータを作成する
     */
    private void createTestTodosForSearching() {
        String[] titles = {
            "Learn Java Programming",
            "Study Spring Framework", 
            "Build REST API",
            "Database Design",
            "Frontend Development",
            "Testing Strategies"
        };

        String[] descriptions = {
            "Complete Java course and practice coding",
            "Master Spring Boot framework for backend development",
            "Create RESTful web services with proper documentation",
            "Design normalized database schema",
            "Learn modern JavaScript and React",
            "Implement comprehensive testing strategies"
        };

        TodoStatus[] statuses = {TodoStatus.TODO, TodoStatus.IN_PROGRESS, TodoStatus.DONE};
        TodoPriority[] priorities = {TodoPriority.LOW, TodoPriority.MEDIUM, TodoPriority.HIGH};

        for (int i = 0; i < titles.length; i++) {
            Todo todo = new Todo();
            todo.setTitle(titles[i]);
            todo.setDescription(descriptions[i]);
            todo.setStatus(statuses[i % statuses.length]);
            todo.setPriority(priorities[i % priorities.length]);
            todo.setDueDate(LocalDate.now().plusDays(i + 1));
            todoRepository.save(todo);
        }
    }
}