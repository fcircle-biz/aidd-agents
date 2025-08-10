package com.example.todoapp.dev;

import com.example.todoapp.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for development environment features.
 * Tests the complete development toolchain and environment-specific functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Transactional
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:test-data.sql")
class DevEnvironmentIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @Test
    void testActuatorHealthEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/actuator/health", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        
        // Check if development-specific health indicator is present
        Map<String, Object> components = (Map<String, Object>) response.getBody().get("components");
        assertNotNull(components);
        assertTrue(components.containsKey("devHealth"));
    }

    @Test
    void testActuatorInfoEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/actuator/info", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> app = (Map<String, Object>) response.getBody().get("app");
        assertNotNull(app);
        assertEquals("Todo Application", app.get("name"));
        assertEquals("Development Environment for Todo Management", app.get("description"));
    }

    @Test
    void testDevDataStatistics() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/data/stats", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify statistics structure
        assertTrue(response.getBody().containsKey("totalTodos"));
        assertTrue(response.getBody().containsKey("completedTodos"));
        assertTrue(response.getBody().containsKey("pendingTodos"));
        assertTrue(response.getBody().containsKey("inProgressTodos"));
    }

    @Test
    void testDevDataReset() {
        // First, ensure we have some data
        long initialCount = todoRepository.count();
        assertTrue(initialCount >= 0);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/dev/data/reset", null, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test data reset successfully", response.getBody().get("message"));
        
        // Verify data was reset and reloaded
        assertTrue(response.getBody().containsKey("deletedCount"));
        assertTrue(response.getBody().containsKey("newCount"));
    }

    @Test
    void testDevMonitoringJvmEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/monitor/jvm", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify JVM metrics structure
        assertTrue(response.getBody().containsKey("runtime"));
        assertTrue(response.getBody().containsKey("memory"));
        assertTrue(response.getBody().containsKey("threads"));
        
        Map<String, Object> memory = (Map<String, Object>) response.getBody().get("memory");
        assertNotNull(memory);
        assertTrue(memory.containsKey("heapUsed"));
        assertTrue(memory.containsKey("heapMax"));
    }

    @Test
    void testDevMonitoringPerformanceEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/monitor/performance", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify performance metrics structure
        assertTrue(response.getBody().containsKey("http"));
        assertTrue(response.getBody().containsKey("database"));
        assertTrue(response.getBody().containsKey("jvm"));
    }

    @Test
    void testDevDiagnosticsEnvironment() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/diagnostics/environment", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify diagnostics structure
        assertTrue(response.getBody().containsKey("activeProfiles"));
        assertTrue(response.getBody().containsKey("systemProperties"));
        assertTrue(response.getBody().containsKey("springConfiguration"));
        
        // Verify dev profile is active
        String[] activeProfiles = (String[]) response.getBody().get("activeProfiles");
        boolean devProfileActive = false;
        for (String profile : activeProfiles) {
            if ("dev".equals(profile)) {
                devProfileActive = true;
                break;
            }
        }
        assertTrue(devProfileActive, "Dev profile should be active");
    }

    @Test
    void testDevDiagnosticsDatabase() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/diagnostics/database", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("connection"));
        
        // Verify database info
        assertTrue(response.getBody().containsKey("databaseInfo"));
        Map<String, Object> dbInfo = (Map<String, Object>) response.getBody().get("databaseInfo");
        assertNotNull(dbInfo);
        assertTrue(dbInfo.containsKey("productName"));
        assertTrue(dbInfo.containsKey("productVersion"));
    }

    @Test
    void testDevApiDocumentationOverview() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/api-docs/overview", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify API documentation structure
        assertEquals("Todo Application REST API", response.getBody().get("title"));
        assertEquals("1.0.0", response.getBody().get("version"));
        assertTrue(response.getBody().containsKey("endpoints"));
    }

    @Test
    void testDevApiDocumentationSpecifications() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/api-docs/specifications", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify specifications structure
        assertTrue(response.getBody().containsKey("models"));
        assertTrue(response.getBody().containsKey("statusCodes"));
        
        Map<String, Object> models = (Map<String, Object>) response.getBody().get("models");
        assertTrue(models.containsKey("TodoRequest"));
        assertTrue(models.containsKey("TodoResponse"));
        assertTrue(models.containsKey("TodoSearchCriteria"));
    }

    @Test
    void testDevUtilsEnvironmentInfo() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/utils/environment-info", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify environment info structure
        assertTrue(response.getBody().containsKey("jvm"));
        assertTrue(response.getBody().containsKey("system"));
        assertTrue(response.getBody().containsKey("memory"));
        
        Map<String, Object> jvm = (Map<String, Object>) response.getBody().get("jvm");
        assertNotNull(jvm);
        assertTrue(jvm.containsKey("version"));
        assertTrue(jvm.containsKey("vendor"));
    }

    @Test
    void testDevUtilsGenerateTestData() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/utils/generate-data/json", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        assertEquals("json", response.getBody().get("type"));
        assertTrue(response.getBody().containsKey("sampleObject"));
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    void testDevDataSeeding() {
        // Clear existing data
        ResponseEntity<Map> clearResponse = restTemplate.exchange(
            "/dev/data/clear", 
            org.springframework.http.HttpMethod.DELETE, 
            null, 
            Map.class);
        
        assertEquals(HttpStatus.OK, clearResponse.getStatusCode());
        assertEquals(0L, todoRepository.count());
        
        // Add test data
        ResponseEntity<Map> seedResponse = restTemplate.postForEntity(
            "/dev/data/seed/5", null, Map.class);
        
        assertEquals(HttpStatus.OK, seedResponse.getStatusCode());
        assertNotNull(seedResponse.getBody());
        assertEquals("Test data added successfully", seedResponse.getBody().get("message"));
        
        // Verify data was added
        long finalCount = todoRepository.count();
        assertEquals(5L, finalCount);
    }

    @Test
    void testDevHealthIndicator() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/actuator/health/devHealth", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        
        Map<String, Object> details = (Map<String, Object>) response.getBody().get("details");
        assertNotNull(details);
        assertEquals("Connected", details.get("database"));
        assertEquals("Development", details.get("environment"));
        assertTrue(details.containsKey("todoCount"));
        assertTrue(details.containsKey("memoryUsagePercent"));
    }

    @Test
    void testErrorHandlingInDevEnvironment() {
        // Test that development error pages are accessible
        // This would typically trigger the dev error controller
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/todos/99999", String.class);
        
        // Should return 404 for non-existent todo
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testActuatorMetricsEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/actuator/metrics", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        assertTrue(response.getBody().containsKey("names"));
        Object[] names = (Object[]) response.getBody().get("names");
        assertNotNull(names);
        assertTrue(names.length > 0);
    }

    @Test
    void testDevConfigurationCheck() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/dev/diagnostics/config-check", Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Verify configuration check structure
        assertTrue(response.getBody().containsKey("profiles"));
        assertTrue(response.getBody().containsKey("criticalConfiguration"));
        assertTrue(response.getBody().containsKey("networking"));
        
        Map<String, Object> profiles = (Map<String, Object>) response.getBody().get("profiles");
        assertEquals(true, profiles.get("isDevelopment"));
    }
}