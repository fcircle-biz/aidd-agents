package com.example.todoapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ログ管理コントローラーのテストクラス
 */
@SpringBootTest
@AutoConfigureMockMvc
class LogManagementControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testGetLogLevels() throws Exception {
        mockMvc.perform(get("/admin/logging/levels"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loggers").isArray())
                .andExpect(jsonPath("$.availableLevels").isArray())
                .andExpect(jsonPath("$.availableLevels").value(containsInAnyOrder("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF")))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    @Test
    void testSetLogLevelValid() throws Exception {
        LogManagementController.LogLevelRequest request = new LogManagementController.LogLevelRequest();
        request.setLevel("DEBUG");
        
        mockMvc.perform(put("/admin/logging/levels/com.example.todoapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggerName").value("com.example.todoapp"))
                .andExpect(jsonPath("$.newLevel").value("DEBUG"))
                .andExpect(jsonPath("$.oldLevel").exists())
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    @Test
    void testSetLogLevelInvalid() throws Exception {
        LogManagementController.LogLevelRequest request = new LogManagementController.LogLevelRequest();
        request.setLevel("INVALID_LEVEL");
        
        mockMvc.perform(put("/admin/logging/levels/com.example.todoapp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("Invalid log level")));
    }
    
    @Test
    void testSetMultipleLogLevels() throws Exception {
        List<LogManagementController.LogLevelBatchRequest> requests = List.of(
                createBatchRequest("com.example.todoapp.service", "DEBUG"),
                createBatchRequest("com.example.todoapp.controller", "INFO"),
                createBatchRequest("invalid.logger.name", "INVALID_LEVEL") // 1つは失敗させる
        );
        
        mockMvc.perform(put("/admin/logging/levels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.results").isArray())
                .andExpect(jsonPath("$.results", hasSize(3)))
                .andExpect(jsonPath("$.summary.total").value(3))
                .andExpect(jsonPath("$.summary.success").value(2))
                .andExpect(jsonPath("$.summary.failure").value(1))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    @Test
    void testResetApplicationLogLevels() throws Exception {
        mockMvc.perform(post("/admin/logging/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resetLoggers").isArray())
                .andExpect(jsonPath("$.resetLoggers", hasItem("com.example.todoapp")))
                .andExpect(jsonPath("$.resetLoggers", hasItem("audit")))
                .andExpect(jsonPath("$.resetLoggers", hasItem("performance")))
                .andExpect(jsonPath("$.defaultLevel").value("DEBUG"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    @Test
    void testGetLoggingInfo() throws Exception {
        mockMvc.perform(get("/admin/logging/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggerContextName").exists())
                .andExpect(jsonPath("$.activeProfiles").exists())
                .andExpect(jsonPath("$.logDirectory").value("logs/"))
                .andExpect(jsonPath("$.availableAppenders").isArray())
                .andExpect(jsonPath("$.availableAppenders", hasItems("CONSOLE", "FILE", "ERROR_FILE", "AUDIT_FILE", "PERFORMANCE_FILE")))
                .andExpect(jsonPath("$.logFiles").isMap())
                .andExpect(jsonPath("$.logFiles.application").value("logs/todo-app.log"))
                .andExpect(jsonPath("$.logFiles.error").value("logs/todo-app-error.log"))
                .andExpect(jsonPath("$.logFiles.audit").value("logs/audit.log"))
                .andExpect(jsonPath("$.logFiles.performance").value("logs/performance.log"))
                .andExpect(jsonPath("$.timestamp").isNumber());
    }
    
    @Test
    void testGetLogLevelsWithSpecificLoggers() throws Exception {
        mockMvc.perform(get("/admin/logging/levels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loggers[?(@.name == 'com.example.todoapp')]").exists())
                .andExpect(jsonPath("$.loggers[?(@.name == 'audit')]").exists())
                .andExpect(jsonPath("$.loggers[?(@.name == 'performance')]").exists())
                .andExpect(jsonPath("$.loggers[?(@.name == 'ROOT')]").exists());
    }
    
    @Test
    void testLogLevelChangeSequence() throws Exception {
        String loggerName = "com.example.todoapp.test";
        
        // 1. 初期状態を確認
        mockMvc.perform(get("/admin/logging/levels"))
                .andExpect(status().isOk());
        
        // 2. DEBUGに変更
        LogManagementController.LogLevelRequest debugRequest = new LogManagementController.LogLevelRequest();
        debugRequest.setLevel("DEBUG");
        
        mockMvc.perform(put("/admin/logging/levels/" + loggerName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(debugRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newLevel").value("DEBUG"));
        
        // 3. INFOに変更
        LogManagementController.LogLevelRequest infoRequest = new LogManagementController.LogLevelRequest();
        infoRequest.setLevel("INFO");
        
        mockMvc.perform(put("/admin/logging/levels/" + loggerName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(infoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oldLevel").value("DEBUG"))
                .andExpect(jsonPath("$.newLevel").value("INFO"));
        
        // 4. リセット
        mockMvc.perform(post("/admin/logging/reset"))
                .andExpect(status().isOk());
    }
    
    private LogManagementController.LogLevelBatchRequest createBatchRequest(String loggerName, String level) {
        LogManagementController.LogLevelBatchRequest request = new LogManagementController.LogLevelBatchRequest();
        request.setLoggerName(loggerName);
        request.setLevel(level);
        return request;
    }
}