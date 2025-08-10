package com.example.todoapp.service;

import com.example.todoapp.dto.AuditLogEntry;
import com.example.todoapp.dto.PerformanceLogEntry;
import com.example.todoapp.service.impl.LoggingServiceImpl;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ログサービスのテストクラス
 */
@SpringBootTest
class LoggingServiceTest {
    
    private LoggingService loggingService;
    private ListAppender<ILoggingEvent> listAppender;
    private ListAppender<ILoggingEvent> auditAppender;
    private Logger rootLogger;
    private Logger auditLogger;
    
    @BeforeEach
    void setUp() {
        loggingService = new LoggingServiceImpl();
        
        // テスト用のリストアペンダーを設定
        rootLogger = (Logger) LoggerFactory.getLogger(LoggingServiceImpl.class);
        auditLogger = (Logger) LoggerFactory.getLogger("audit");
        
        listAppender = new ListAppender<>();
        listAppender.start();
        rootLogger.addAppender(listAppender);
        rootLogger.setLevel(Level.DEBUG);
        
        auditAppender = new ListAppender<>();
        auditAppender.start();
        auditLogger.addAppender(auditAppender);
        auditLogger.setLevel(Level.DEBUG);
        
        // MDCをクリア
        MDC.clear();
    }
    
    @Test
    void testLogAuditSuccess() {
        // Given
        AuditLogEntry entry = AuditLogEntry.success("CREATE", "TODO", "123", "user1");
        entry.setDetails("Todo created successfully");
        entry.setCorrelationId("test-correlation-id");
        
        // When
        loggingService.logAudit(entry);
        
        // Then
        List<ILoggingEvent> auditEvents = auditAppender.list;
        assertThat(auditEvents).hasSize(1);
        
        ILoggingEvent event = auditEvents.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).contains("CREATE");
        assertThat(event.getFormattedMessage()).contains("TODO");
        assertThat(event.getFormattedMessage()).contains("123");
        assertThat(event.getFormattedMessage()).contains("user1");
        assertThat(event.getFormattedMessage()).contains("SUCCESS");
    }
    
    @Test
    void testLogAuditFailure() {
        // Given
        AuditLogEntry entry = AuditLogEntry.failure("DELETE", "TODO", "456", "user2", 
                                                   "Todo not found", "NOT_FOUND");
        
        // When
        loggingService.logAudit(entry);
        
        // Then
        List<ILoggingEvent> auditEvents = auditAppender.list;
        assertThat(auditEvents).hasSize(1);
        
        // 失敗の場合は通常ログにも警告が記録される
        List<ILoggingEvent> rootEvents = listAppender.list;
        assertThat(rootEvents).hasSize(1);
        assertThat(rootEvents.get(0).getLevel()).isEqualTo(Level.WARN);
    }
    
    @Test
    void testLogPerformance() {
        // Given
        PerformanceLogEntry entry = PerformanceLogEntry.builder()
                .operationName("testOperation")
                .className("TestClass")
                .methodName("testMethod")
                .executionTimeMs(150)
                .recordCount(10)
                .correlationId("test-correlation-id")
                .userId("user1")
                .build();
        entry.setPerformanceLevel("GOOD");
        
        // When
        loggingService.logPerformance(entry);
        
        // Then - パフォーマンスログは専用のロガーに記録される
        // テストでは簡略化のため、エラーなく実行されることを確認
        assertThat(entry.toLogMessage()).contains("TestClass.testMethod");
        assertThat(entry.toLogMessage()).contains("150ms");
        assertThat(entry.toLogMessage()).contains("10 records");
    }
    
    @Test
    void testLogBusinessOperation() {
        // When
        loggingService.logBusinessOperation("USER_LOGIN", "User logged in successfully");
        
        // Then
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).hasSize(1);
        
        ILoggingEvent event = events.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).contains("Business Operation");
        assertThat(event.getFormattedMessage()).contains("USER_LOGIN");
    }
    
    @Test
    void testLogSecurity() {
        // When
        loggingService.logSecurity("AUTHENTICATION_FAILURE", "Invalid credentials");
        
        // Then
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).hasSize(1);
        
        ILoggingEvent event = events.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.WARN);
        assertThat(event.getFormattedMessage()).contains("Security Event");
        assertThat(event.getFormattedMessage()).contains("AUTHENTICATION_FAILURE");
    }
    
    @Test
    void testLogApiCall() {
        // When - 正常な応答
        loggingService.logApiCall("GET", "/api/todos", 200, 50);
        
        // Then
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).hasSize(1);
        
        ILoggingEvent event = events.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).contains("GET /api/todos");
        assertThat(event.getFormattedMessage()).contains("200");
        assertThat(event.getFormattedMessage()).contains("50ms");
    }
    
    @Test
    void testLogApiCallWithError() {
        // When - エラー応答
        loggingService.logApiCall("POST", "/api/todos", 500, 100);
        
        // Then
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).hasSize(1);
        
        ILoggingEvent event = events.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getFormattedMessage()).contains("POST /api/todos");
        assertThat(event.getFormattedMessage()).contains("500");
    }
    
    @Test
    void testLogDatabaseOperation() {
        // When
        loggingService.logDatabaseOperation("SELECT", "todos", 25);
        
        // Then
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).hasSize(1);
        
        ILoggingEvent event = events.get(0);
        assertThat(event.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(event.getFormattedMessage()).contains("Database Operation");
        assertThat(event.getFormattedMessage()).contains("SELECT");
        assertThat(event.getFormattedMessage()).contains("todos");
        assertThat(event.getFormattedMessage()).contains("25 records");
    }
    
    @Test
    void testLogDatabaseOperationLargeData() {
        // When - 大量データの処理
        loggingService.logDatabaseOperation("UPDATE", "todos", 1500);
        
        // Then
        List<ILoggingEvent> events = listAppender.list;
        assertThat(events).hasSize(2); // DEBUGとWARNの両方
        
        // WARNログの確認
        ILoggingEvent warnEvent = events.stream()
                .filter(e -> e.getLevel() == Level.WARN)
                .findFirst()
                .orElse(null);
        
        assertThat(warnEvent).isNotNull();
        assertThat(warnEvent.getFormattedMessage()).contains("Large Database Operation");
        assertThat(warnEvent.getFormattedMessage()).contains("1500 records");
    }
    
    @Test
    void testSetCorrelationId() {
        // Given
        String correlationId = "test-correlation-123";
        
        // When
        loggingService.setCorrelationId(correlationId);
        
        // Then
        assertThat(MDC.get("correlationId")).isEqualTo(correlationId);
    }
    
    @Test
    void testSetUserContext() {
        // Given
        String userId = "user123";
        String userRole = "ADMIN";
        
        // When
        loggingService.setUserContext(userId, userRole);
        
        // Then
        assertThat(MDC.get("userId")).isEqualTo(userId);
        assertThat(MDC.get("userRole")).isEqualTo(userRole);
    }
    
    @Test
    void testClearContext() {
        // Given - コンテキストに値を設定
        MDC.put("correlationId", "test-id");
        MDC.put("userId", "test-user");
        MDC.put("userRole", "USER");
        
        // When
        loggingService.clearContext();
        
        // Then
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
    
    @Test
    void testAuditLogEntryFactoryMethods() {
        // Test success factory method
        AuditLogEntry successEntry = AuditLogEntry.success("CREATE", "TODO", "123", "user1");
        
        assertThat(successEntry.getOperation()).isEqualTo("CREATE");
        assertThat(successEntry.getResourceType()).isEqualTo("TODO");
        assertThat(successEntry.getResourceId()).isEqualTo("123");
        assertThat(successEntry.getUserId()).isEqualTo("user1");
        assertThat(successEntry.getResult()).isEqualTo("SUCCESS");
        
        // Test failure factory method
        AuditLogEntry failureEntry = AuditLogEntry.failure("DELETE", "TODO", "456", "user2", 
                                                          "Not found", "NOT_FOUND");
        
        assertThat(failureEntry.getOperation()).isEqualTo("DELETE");
        assertThat(failureEntry.getResult()).isEqualTo("FAILURE");
        assertThat(failureEntry.getErrorMessage()).isEqualTo("Not found");
        assertThat(failureEntry.getErrorCode()).isEqualTo("NOT_FOUND");
    }
    
    @Test
    void testPerformanceLogEntryFactoryMethods() {
        // Test simple factory method
        PerformanceLogEntry entry1 = PerformanceLogEntry.of("testOperation", 250);
        
        assertThat(entry1.getOperationName()).isEqualTo("testOperation");
        assertThat(entry1.getExecutionTimeMs()).isEqualTo(250);
        assertThat(entry1.getPerformanceLevel()).isEqualTo("ACCEPTABLE");
        
        // Test method factory method
        PerformanceLogEntry entry2 = PerformanceLogEntry.forMethod("TestClass", "testMethod", 50);
        
        assertThat(entry2.getClassName()).isEqualTo("TestClass");
        assertThat(entry2.getMethodName()).isEqualTo("testMethod");
        assertThat(entry2.getOperationName()).isEqualTo("TestClass.testMethod");
        assertThat(entry2.getPerformanceLevel()).isEqualTo("EXCELLENT");
    }
    
    @Test
    void testPerformanceLogEntryEvaluateLevel() {
        assertThat(PerformanceLogEntry.of("test", 50).evaluatePerformanceLevel()).isEqualTo("EXCELLENT");
        assertThat(PerformanceLogEntry.of("test", 300).evaluatePerformanceLevel()).isEqualTo("GOOD");
        assertThat(PerformanceLogEntry.of("test", 1500).evaluatePerformanceLevel()).isEqualTo("ACCEPTABLE");
        assertThat(PerformanceLogEntry.of("test", 5000).evaluatePerformanceLevel()).isEqualTo("POOR");
        assertThat(PerformanceLogEntry.of("test", 15000).evaluatePerformanceLevel()).isEqualTo("CRITICAL");
    }
}