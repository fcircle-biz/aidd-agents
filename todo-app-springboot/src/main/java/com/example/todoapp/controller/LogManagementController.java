package com.example.todoapp.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ログレベル管理コントローラー
 * 実行時のログレベル動的変更機能を提供
 */
@RestController
@RequestMapping("/admin/logging")
@Slf4j
public class LogManagementController {
    
    /**
     * 全ロガーのログレベル一覧を取得
     */
    @GetMapping("/levels")
    public ResponseEntity<Map<String, Object>> getLogLevels() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            List<LoggerInfo> loggers = new ArrayList<>();
            
            // アプリケーションロガーの取得
            for (Logger logger : loggerContext.getLoggerList()) {
                if (logger.getName().startsWith("com.example.todoapp") || 
                    logger.getName().equals("ROOT") || 
                    logger.getName().equals("audit") || 
                    logger.getName().equals("performance")) {
                    loggers.add(new LoggerInfo(
                        logger.getName(), 
                        logger.getEffectiveLevel().toString(),
                        logger.getLevel() != null ? logger.getLevel().toString() : "INHERITED"
                    ));
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("loggers", loggers);
            response.put("availableLevels", List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "OFF"));
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("Log levels requested - {} loggers returned", loggers.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to get log levels", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get log levels: " + e.getMessage()));
        }
    }
    
    /**
     * 特定のロガーのログレベルを変更
     */
    @PutMapping("/levels/{loggerName}")
    public ResponseEntity<Map<String, Object>> setLogLevel(
            @PathVariable String loggerName,
            @RequestBody LogLevelRequest request) {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger logger = loggerContext.getLogger(loggerName);
            
            String oldLevel = logger.getEffectiveLevel().toString();
            Level newLevel = Level.valueOf(request.getLevel().toUpperCase());
            
            logger.setLevel(newLevel);
            
            Map<String, Object> response = new HashMap<>();
            response.put("loggerName", loggerName);
            response.put("oldLevel", oldLevel);
            response.put("newLevel", newLevel.toString());
            response.put("timestamp", System.currentTimeMillis());
            
            log.warn("Log level changed: {} from {} to {}", loggerName, oldLevel, newLevel);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid log level: {}", request.getLevel(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid log level: " + request.getLevel()));
        } catch (Exception e) {
            log.error("Failed to set log level for {}", loggerName, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to set log level: " + e.getMessage()));
        }
    }
    
    /**
     * 複数のロガーのログレベルを一括変更
     */
    @PutMapping("/levels")
    public ResponseEntity<Map<String, Object>> setMultipleLogLevels(
            @RequestBody List<LogLevelBatchRequest> requests) {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            List<Map<String, Object>> results = new ArrayList<>();
            int successCount = 0;
            int failureCount = 0;
            
            for (LogLevelBatchRequest request : requests) {
                try {
                    Logger logger = loggerContext.getLogger(request.getLoggerName());
                    String oldLevel = logger.getEffectiveLevel().toString();
                    Level newLevel = Level.valueOf(request.getLevel().toUpperCase());
                    
                    logger.setLevel(newLevel);
                    
                    results.add(Map.of(
                            "loggerName", request.getLoggerName(),
                            "oldLevel", oldLevel,
                            "newLevel", newLevel.toString(),
                            "status", "SUCCESS"
                    ));
                    successCount++;
                    
                } catch (Exception e) {
                    results.add(Map.of(
                            "loggerName", request.getLoggerName(),
                            "error", e.getMessage(),
                            "status", "FAILED"
                    ));
                    failureCount++;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("summary", Map.of(
                    "total", requests.size(),
                    "success", successCount,
                    "failure", failureCount
            ));
            response.put("timestamp", System.currentTimeMillis());
            
            log.warn("Batch log level change completed: {} success, {} failures", successCount, failureCount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to process batch log level changes", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process batch changes: " + e.getMessage()));
        }
    }
    
    /**
     * アプリケーション固有のロガーのログレベルをリセット
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetApplicationLogLevels() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            List<String> resetLoggers = new ArrayList<>();
            
            // アプリケーションロガーをDEBUGレベルにリセット
            String[] appLoggers = {
                    "com.example.todoapp",
                    "com.example.todoapp.controller",
                    "com.example.todoapp.service",
                    "com.example.todoapp.repository"
            };
            
            for (String loggerName : appLoggers) {
                Logger logger = loggerContext.getLogger(loggerName);
                logger.setLevel(Level.DEBUG);
                resetLoggers.add(loggerName);
            }
            
            // 特別なロガーの設定
            loggerContext.getLogger("audit").setLevel(Level.INFO);
            loggerContext.getLogger("performance").setLevel(Level.INFO);
            resetLoggers.add("audit");
            resetLoggers.add("performance");
            
            Map<String, Object> response = new HashMap<>();
            response.put("resetLoggers", resetLoggers);
            response.put("defaultLevel", "DEBUG");
            response.put("timestamp", System.currentTimeMillis());
            
            log.warn("Application log levels reset to default values");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to reset log levels", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to reset log levels: " + e.getMessage()));
        }
    }
    
    /**
     * 現在の環境情報とログ設定状況を取得
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getLoggingInfo() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            Map<String, Object> info = new HashMap<>();
            info.put("loggerContextName", loggerContext.getName());
            info.put("configurationFile", System.getProperty("logback.configurationFile"));
            info.put("activeProfiles", System.getProperty("spring.profiles.active", "default"));
            info.put("logDirectory", "logs/");
            info.put("availableAppenders", List.of("CONSOLE", "FILE", "ERROR_FILE", "AUDIT_FILE", "PERFORMANCE_FILE"));
            
            // ログファイルの状態
            Map<String, Object> logFiles = new HashMap<>();
            logFiles.put("application", "logs/todo-app.log");
            logFiles.put("error", "logs/todo-app-error.log");
            logFiles.put("audit", "logs/audit.log");
            logFiles.put("performance", "logs/performance.log");
            info.put("logFiles", logFiles);
            
            info.put("timestamp", System.currentTimeMillis());
            
            log.debug("Logging info requested");
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("Failed to get logging info", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get logging info: " + e.getMessage()));
        }
    }
    
    // DTOクラス
    @Data
    public static class LoggerInfo {
        private String name;
        private String effectiveLevel;
        private String configuredLevel;
        
        public LoggerInfo(String name, String effectiveLevel, String configuredLevel) {
            this.name = name;
            this.effectiveLevel = effectiveLevel;
            this.configuredLevel = configuredLevel;
        }
    }
    
    @Data
    public static class LogLevelRequest {
        private String level;
    }
    
    @Data
    public static class LogLevelBatchRequest {
        private String loggerName;
        private String level;
    }
}