package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoResponse;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Development API documentation and testing controller.
 * Provides API documentation, examples, and testing endpoints for development.
 */
@RestController
@RequestMapping("/dev/api-docs")
@Profile("dev")
public class DevApiDocController {

    /**
     * Get API documentation overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getApiOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        overview.put("title", "Todo Application REST API");
        overview.put("version", "1.0.0");
        overview.put("description", "Comprehensive REST API for Todo management with full CRUD operations");
        overview.put("baseUrl", "/api");
        
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Development Team");
        contact.put("environment", "Development");
        overview.put("contact", contact);
        
        // API endpoints documentation
        List<Map<String, Object>> endpoints = new ArrayList<>();
        
        endpoints.add(createEndpointDoc("GET", "/api/todos", "Get all todos", "Retrieve all todo items with optional pagination"));
        endpoints.add(createEndpointDoc("GET", "/api/todos/{id}", "Get todo by ID", "Retrieve a specific todo item by its ID"));
        endpoints.add(createEndpointDoc("POST", "/api/todos", "Create todo", "Create a new todo item"));
        endpoints.add(createEndpointDoc("PUT", "/api/todos/{id}", "Update todo", "Update an existing todo item"));
        endpoints.add(createEndpointDoc("DELETE", "/api/todos/{id}", "Delete todo", "Delete a todo item by its ID"));
        endpoints.add(createEndpointDoc("GET", "/api/todos/search", "Search todos", "Search todo items with various criteria"));
        
        overview.put("endpoints", endpoints);
        
        return ResponseEntity.ok(overview);
    }

    /**
     * Get detailed API specifications
     */
    @GetMapping("/specifications")
    public ResponseEntity<Map<String, Object>> getApiSpecifications() {
        Map<String, Object> specs = new HashMap<>();
        
        // Data models
        Map<String, Object> models = new HashMap<>();
        
        // TodoRequest model
        Map<String, Object> todoRequest = new HashMap<>();
        todoRequest.put("description", "Request model for creating and updating todos");
        todoRequest.put("required", Arrays.asList("title"));
        
        Map<String, Object> todoRequestFields = new HashMap<>();
        todoRequestFields.put("title", createFieldDoc("string", "Todo title", "1-200 characters", true));
        todoRequestFields.put("description", createFieldDoc("string", "Todo description", "Up to 1000 characters", false));
        todoRequestFields.put("dueDate", createFieldDoc("date", "Due date", "ISO date format (YYYY-MM-DD)", false));
        todoRequestFields.put("priority", createFieldDoc("enum", "Priority level", "HIGH, MEDIUM, LOW", false));
        todoRequestFields.put("status", createFieldDoc("enum", "Todo status", "PENDING, IN_PROGRESS, COMPLETED", false));
        todoRequest.put("fields", todoRequestFields);
        models.put("TodoRequest", todoRequest);
        
        // TodoResponse model
        Map<String, Object> todoResponse = new HashMap<>();
        todoResponse.put("description", "Response model for todo data");
        Map<String, Object> todoResponseFields = new HashMap<>();
        todoResponseFields.put("id", createFieldDoc("long", "Unique identifier", "Auto-generated", false));
        todoResponseFields.put("title", createFieldDoc("string", "Todo title", null, false));
        todoResponseFields.put("description", createFieldDoc("string", "Todo description", null, false));
        todoResponseFields.put("dueDate", createFieldDoc("date", "Due date", "ISO date format", false));
        todoResponseFields.put("priority", createFieldDoc("enum", "Priority level", "HIGH, MEDIUM, LOW", false));
        todoResponseFields.put("status", createFieldDoc("enum", "Todo status", "PENDING, IN_PROGRESS, COMPLETED", false));
        todoResponseFields.put("createdAt", createFieldDoc("datetime", "Creation timestamp", "ISO datetime format", false));
        todoResponseFields.put("updatedAt", createFieldDoc("datetime", "Last update timestamp", "ISO datetime format", false));
        todoResponse.put("fields", todoResponseFields);
        models.put("TodoResponse", todoResponse);
        
        // TodoSearchCriteria model
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("description", "Search criteria for filtering todos");
        Map<String, Object> searchFields = new HashMap<>();
        searchFields.put("keyword", createFieldDoc("string", "Search keyword", "Searches in title and description", false));
        searchFields.put("status", createFieldDoc("enum", "Filter by status", "PENDING, IN_PROGRESS, COMPLETED", false));
        searchFields.put("priority", createFieldDoc("enum", "Filter by priority", "HIGH, MEDIUM, LOW", false));
        searchFields.put("dueDateFrom", createFieldDoc("date", "Due date range start", "ISO date format", false));
        searchFields.put("dueDateTo", createFieldDoc("date", "Due date range end", "ISO date format", false));
        searchFields.put("createdDateFrom", createFieldDoc("date", "Creation date range start", "ISO date format", false));
        searchFields.put("createdDateTo", createFieldDoc("date", "Creation date range end", "ISO date format", false));
        searchCriteria.put("fields", searchFields);
        models.put("TodoSearchCriteria", searchCriteria);
        
        specs.put("models", models);
        
        // HTTP status codes
        Map<String, Object> statusCodes = new HashMap<>();
        statusCodes.put("200", "OK - Request successful");
        statusCodes.put("201", "Created - Resource created successfully");
        statusCodes.put("400", "Bad Request - Invalid input data");
        statusCodes.put("404", "Not Found - Resource not found");
        statusCodes.put("500", "Internal Server Error - Server error occurred");
        specs.put("statusCodes", statusCodes);
        
        return ResponseEntity.ok(specs);
    }

    /**
     * Get API usage examples
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getApiExamples() {
        Map<String, Object> examples = new HashMap<>();
        
        // Request examples
        Map<String, Object> requestExamples = new HashMap<>();
        
        // Create todo example
        Map<String, Object> createExample = new HashMap<>();
        createExample.put("url", "POST /api/todos");
        createExample.put("headers", Map.of("Content-Type", "application/json"));
        
        TodoRequest createRequest = new TodoRequest();
        createRequest.setTitle("新しいタスクを作成する");
        createRequest.setDescription("APIテスト用のサンプルタスクです");
        createRequest.setDueDate(LocalDate.now().plusDays(7));
        createRequest.setPriority(TodoPriority.HIGH);
        createRequest.setStatus(TodoStatus.PENDING);
        createExample.put("body", createRequest);
        requestExamples.put("createTodo", createExample);
        
        // Update todo example
        Map<String, Object> updateExample = new HashMap<>();
        updateExample.put("url", "PUT /api/todos/1");
        updateExample.put("headers", Map.of("Content-Type", "application/json"));
        
        TodoRequest updateRequest = new TodoRequest();
        updateRequest.setTitle("更新されたタスク");
        updateRequest.setDescription("このタスクは更新されました");
        updateRequest.setStatus(TodoStatus.IN_PROGRESS);
        updateExample.put("body", updateRequest);
        requestExamples.put("updateTodo", updateExample);
        
        // Search example
        Map<String, Object> searchExample = new HashMap<>();
        searchExample.put("url", "GET /api/todos/search");
        Map<String, Object> searchParams = new HashMap<>();
        searchParams.put("keyword", "重要");
        searchParams.put("status", "PENDING");
        searchParams.put("priority", "HIGH");
        searchParams.put("dueDateFrom", "2024-01-01");
        searchParams.put("dueDateTo", "2024-12-31");
        searchExample.put("queryParams", searchParams);
        requestExamples.put("searchTodos", searchExample);
        
        examples.put("requests", requestExamples);
        
        // Response examples
        Map<String, Object> responseExamples = new HashMap<>();
        
        // Success response example
        TodoResponse successResponse = new TodoResponse();
        successResponse.setId(1L);
        successResponse.setTitle("サンプルタスク");
        successResponse.setDescription("これはサンプルのタスクです");
        successResponse.setDueDate(LocalDate.now().plusDays(5));
        successResponse.setPriority(TodoPriority.MEDIUM);
        successResponse.setStatus(TodoStatus.PENDING);
        responseExamples.put("successResponse", successResponse);
        
        // Error response example
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", "2024-01-01T12:00:00");
        errorResponse.put("status", 400);
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", "Validation failed");
        errorResponse.put("path", "/api/todos");
        
        List<Map<String, String>> validationErrors = new ArrayList<>();
        validationErrors.add(Map.of("field", "title", "message", "タイトルは必須です"));
        validationErrors.add(Map.of("field", "dueDate", "message", "過去の日付は設定できません"));
        errorResponse.put("validationErrors", validationErrors);
        responseExamples.put("errorResponse", errorResponse);
        
        examples.put("responses", responseExamples);
        
        return ResponseEntity.ok(examples);
    }

    /**
     * Get sample test data
     */
    @GetMapping("/test-data")
    public ResponseEntity<Map<String, Object>> getTestData() {
        Map<String, Object> testData = new HashMap<>();
        
        // Sample todo requests for testing
        List<TodoRequest> sampleRequests = new ArrayList<>();
        
        TodoRequest request1 = new TodoRequest();
        request1.setTitle("高優先度タスク");
        request1.setDescription("緊急に対応が必要なタスクです");
        request1.setDueDate(LocalDate.now().plusDays(1));
        request1.setPriority(TodoPriority.HIGH);
        request1.setStatus(TodoStatus.PENDING);
        sampleRequests.add(request1);
        
        TodoRequest request2 = new TodoRequest();
        request2.setTitle("通常業務");
        request2.setDescription("日常的な業務タスクです");
        request2.setDueDate(LocalDate.now().plusDays(7));
        request2.setPriority(TodoPriority.MEDIUM);
        request2.setStatus(TodoStatus.IN_PROGRESS);
        sampleRequests.add(request2);
        
        TodoRequest request3 = new TodoRequest();
        request3.setTitle("長期プロジェクト");
        request3.setDescription("将来的に取り組む予定のプロジェクトです");
        request3.setDueDate(LocalDate.now().plusDays(30));
        request3.setPriority(TodoPriority.LOW);
        request3.setStatus(TodoStatus.PENDING);
        sampleRequests.add(request3);
        
        testData.put("sampleRequests", sampleRequests);
        
        // Sample search criteria
        List<TodoSearchCriteria> sampleSearches = new ArrayList<>();
        
        TodoSearchCriteria search1 = new TodoSearchCriteria();
        search1.setKeyword("重要");
        search1.setStatus(TodoStatus.PENDING);
        sampleSearches.add(search1);
        
        TodoSearchCriteria search2 = new TodoSearchCriteria();
        search2.setPriority(TodoPriority.HIGH);
        search2.setDueDateFrom(LocalDate.now());
        search2.setDueDateTo(LocalDate.now().plusDays(7));
        sampleSearches.add(search2);
        
        testData.put("sampleSearches", sampleSearches);
        
        // Test scenarios
        List<Map<String, Object>> testScenarios = new ArrayList<>();
        
        testScenarios.add(Map.of(
            "name", "基本的なTodo作成テスト",
            "description", "正常なデータでTodoを作成するテスト",
            "method", "POST",
            "url", "/api/todos",
            "expectedStatus", 201
        ));
        
        testScenarios.add(Map.of(
            "name", "バリデーションエラーテスト",
            "description", "不正なデータでバリデーションエラーを発生させるテスト",
            "method", "POST",
            "url", "/api/todos",
            "expectedStatus", 400
        ));
        
        testScenarios.add(Map.of(
            "name", "存在しないTodo取得テスト",
            "description", "存在しないIDで404エラーを確認するテスト",
            "method", "GET",
            "url", "/api/todos/99999",
            "expectedStatus", 404
        ));
        
        testData.put("testScenarios", testScenarios);
        
        return ResponseEntity.ok(testData);
    }

    /**
     * API testing helper - validate request structure
     */
    @PostMapping("/validate-request")
    public ResponseEntity<Map<String, Object>> validateRequest(@RequestBody Map<String, Object> request) {
        Map<String, Object> validation = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check required fields for TodoRequest
        if (!request.containsKey("title") || request.get("title") == null || request.get("title").toString().trim().isEmpty()) {
            errors.add("title field is required and cannot be empty");
        }
        
        // Validate title length
        if (request.containsKey("title") && request.get("title") != null) {
            String title = request.get("title").toString();
            if (title.length() > 200) {
                errors.add("title cannot exceed 200 characters");
            }
        }
        
        // Validate description length
        if (request.containsKey("description") && request.get("description") != null) {
            String description = request.get("description").toString();
            if (description.length() > 1000) {
                errors.add("description cannot exceed 1000 characters");
            }
        }
        
        // Validate enum values
        if (request.containsKey("priority") && request.get("priority") != null) {
            String priority = request.get("priority").toString();
            if (!Arrays.asList("HIGH", "MEDIUM", "LOW").contains(priority)) {
                errors.add("priority must be one of: HIGH, MEDIUM, LOW");
            }
        }
        
        if (request.containsKey("status") && request.get("status") != null) {
            String status = request.get("status").toString();
            if (!Arrays.asList("PENDING", "IN_PROGRESS", "COMPLETED").contains(status)) {
                errors.add("status must be one of: PENDING, IN_PROGRESS, COMPLETED");
            }
        }
        
        // Check for additional fields that might be typos
        Set<String> validFields = Set.of("title", "description", "dueDate", "priority", "status");
        for (String key : request.keySet()) {
            if (!validFields.contains(key)) {
                warnings.add("Unknown field: " + key + " (will be ignored)");
            }
        }
        
        validation.put("valid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("requestStructure", request);
        
        return ResponseEntity.ok(validation);
    }

    private Map<String, Object> createEndpointDoc(String method, String path, String summary, String description) {
        Map<String, Object> endpoint = new HashMap<>();
        endpoint.put("method", method);
        endpoint.put("path", path);
        endpoint.put("summary", summary);
        endpoint.put("description", description);
        return endpoint;
    }

    private Map<String, Object> createFieldDoc(String type, String description, String constraints, boolean required) {
        Map<String, Object> field = new HashMap<>();
        field.put("type", type);
        field.put("description", description);
        if (constraints != null) field.put("constraints", constraints);
        field.put("required", required);
        return field;
    }
}