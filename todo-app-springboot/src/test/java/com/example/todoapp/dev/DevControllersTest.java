package com.example.todoapp.dev;

import com.example.todoapp.repository.TodoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for development-specific controllers.
 * Tests individual controller endpoints and their functionality.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("dev")
@Transactional
class DevControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @Test
    void testDevDataControllerStats() throws Exception {
        mockMvc.perform(get("/dev/data/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalTodos").exists())
                .andExpect(jsonPath("$.completedTodos").exists())
                .andExpect(jsonPath("$.pendingTodos").exists())
                .andExpect(jsonPath("$.inProgressTodos").exists());
    }

    @Test
    void testDevDataControllerAddTestData() throws Exception {
        mockMvc.perform(post("/dev/data/seed/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Test data added successfully"))
                .andExpect(jsonPath("$.addedCount").value(3));
    }

    @Test
    void testDevDataControllerAddTestDataInvalidCount() throws Exception {
        mockMvc.perform(post("/dev/data/seed/150"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Count must be between 1 and 100"));
    }

    @Test
    void testDevDataControllerCreateOverdueTodos() throws Exception {
        mockMvc.perform(post("/dev/data/create-overdue/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Overdue todos created successfully"))
                .andExpect(jsonPath("$.createdCount").value(5));
    }

    @Test
    void testDevMonitoringControllerJvm() throws Exception {
        mockMvc.perform(get("/dev/monitor/jvm"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.runtime").exists())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.threads").exists())
                .andExpect(jsonPath("$.runtime.uptime").exists())
                .andExpect(jsonPath("$.memory.heapUsed").exists())
                .andExpect(jsonPath("$.threads.threadCount").exists());
    }

    @Test
    void testDevMonitoringControllerPerformance() throws Exception {
        mockMvc.perform(get("/dev/monitor/performance"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.http").exists())
                .andExpect(jsonPath("$.database").exists())
                .andExpect(jsonPath("$.jvm").exists());
    }

    @Test
    void testDevMonitoringControllerSystem() throws Exception {
        mockMvc.perform(get("/dev/monitor/system"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.availableProcessors").exists())
                .andExpect(jsonPath("$.runtime").exists())
                .andExpect(jsonPath("$.operatingSystem").exists())
                .andExpect(jsonPath("$.java").exists());
    }

    @Test
    void testDevDiagnosticsControllerEnvironment() throws Exception {
        mockMvc.perform(get("/dev/diagnostics/environment"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.activeProfiles").exists())
                .andExpect(jsonPath("$.systemProperties").exists())
                .andExpect(jsonPath("$.springConfiguration").exists());
    }

    @Test
    void testDevDiagnosticsControllerDatabase() throws Exception {
        mockMvc.perform(get("/dev/diagnostics/database"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.connection").value("SUCCESS"))
                .andExpect(jsonPath("$.databaseInfo").exists());
    }

    @Test
    void testDevApiDocControllerOverview() throws Exception {
        mockMvc.perform(get("/dev/api-docs/overview"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Todo Application REST API"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.endpoints").exists());
    }

    @Test
    void testDevApiDocControllerSpecifications() throws Exception {
        mockMvc.perform(get("/dev/api-docs/specifications"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.models").exists())
                .andExpect(jsonPath("$.statusCodes").exists())
                .andExpect(jsonPath("$.models.TodoRequest").exists())
                .andExpect(jsonPath("$.models.TodoResponse").exists());
    }

    @Test
    void testDevApiDocControllerExamples() throws Exception {
        mockMvc.perform(get("/dev/api-docs/examples"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.requests").exists())
                .andExpect(jsonPath("$.responses").exists());
    }

    @Test
    void testDevApiDocControllerValidateRequest() throws Exception {
        Map<String, Object> validRequest = new HashMap<>();
        validRequest.put("title", "Valid Todo Title");
        validRequest.put("description", "Valid description");
        validRequest.put("priority", "HIGH");
        validRequest.put("status", "PENDING");

        mockMvc.perform(post("/dev/api-docs/validate-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void testDevApiDocControllerValidateInvalidRequest() throws Exception {
        Map<String, Object> invalidRequest = new HashMap<>();
        invalidRequest.put("description", "Missing title");
        invalidRequest.put("priority", "INVALID_PRIORITY");

        mockMvc.perform(post("/dev/api-docs/validate-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(2));
    }

    @Test
    void testDevUtilsControllerEnvironmentInfo() throws Exception {
        mockMvc.perform(get("/dev/utils/environment-info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jvm").exists())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.memory").exists());
    }

    @Test
    void testDevUtilsControllerGenerateJsonData() throws Exception {
        mockMvc.perform(get("/dev/utils/generate-data/json"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value("json"))
                .andExpect(jsonPath("$.sampleObject").exists());
    }

    @Test
    void testDevUtilsControllerGenerateInvalidDataType() throws Exception {
        mockMvc.perform(get("/dev/utils/generate-data/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Unknown data type"));
    }

    @Test
    void testDevUtilsControllerTestLogging() throws Exception {
        Map<String, Object> logMessage = new HashMap<>();
        logMessage.put("message", "Test debug message");

        mockMvc.perform(post("/dev/utils/test-logging/debug")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logMessage)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.level").value("DEBUG"))
                .andExpect(jsonPath("$.status").value("Log message sent successfully"));
    }

    @Test
    void testDevUtilsControllerTestInvalidLogging() throws Exception {
        mockMvc.perform(post("/dev/utils/test-logging/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testDevUtilsControllerMemoryStressTest() throws Exception {
        mockMvc.perform(post("/dev/utils/stress-test/memory/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("Memory stress test completed successfully"))
                .andExpect(jsonPath("$.allocatedMB").value(10));
    }

    @Test
    void testDevUtilsControllerInvalidMemoryStressTest() throws Exception {
        mockMvc.perform(post("/dev/utils/stress-test/memory/200"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Size must be between 1 and 100 MB"));
    }

    @Test
    void testDevUtilsControllerCpuStressTest() throws Exception {
        mockMvc.perform(post("/dev/utils/stress-test/cpu/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("CPU stress test completed successfully"))
                .andExpect(jsonPath("$.requestedDurationSeconds").value(2));
    }

    @Test
    void testDevUtilsControllerInvalidCpuStressTest() throws Exception {
        mockMvc.perform(post("/dev/utils/stress-test/cpu/50"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Duration must be between 1 and 30 seconds"));
    }
}