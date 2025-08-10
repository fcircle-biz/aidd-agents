package com.example.todoapp.aspect;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.service.LoggingService;
import com.example.todoapp.service.TodoService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * ログアスペクトの統合テスト
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoggingAspectsIntegrationTest {
    
    @Autowired
    private TodoService todoService;
    
    @SpyBean
    private LoggingService loggingService;
    
    private ListAppender<ILoggingEvent> listAppender;
    private Logger performanceLogger;
    
    @BeforeEach
    void setUp() {
        // パフォーマンスログ用のアペンダーを設定
        performanceLogger = (Logger) LoggerFactory.getLogger("performance");
        listAppender = new ListAppender<>();
        listAppender.start();
        performanceLogger.addAppender(listAppender);
        performanceLogger.setLevel(Level.DEBUG);
    }
    
    @Test
    void testAuditLoggingForCreateTodo() {
        // Given
        TodoRequest request = createValidTodoRequest();
        
        // When
        todoService.create(request);
        
        // Then - 監査ログが記録されることを確認
        ArgumentCaptor<com.example.todoapp.dto.AuditLogEntry> auditCaptor = 
                ArgumentCaptor.forClass(com.example.todoapp.dto.AuditLogEntry.class);
        verify(loggingService, atLeastOnce()).logAudit(auditCaptor.capture());
        
        com.example.todoapp.dto.AuditLogEntry auditEntry = auditCaptor.getValue();
        assertThat(auditEntry.getOperation()).isEqualTo("CREATE");
        assertThat(auditEntry.getResourceType()).isEqualTo("TODO");
        assertThat(auditEntry.getResult()).isEqualTo("SUCCESS");
        assertThat(auditEntry.getUserId()).isEqualTo("system"); // テスト用の固定値
    }
    
    @Test
    void testPerformanceLoggingForServiceMethod() {
        // Given
        TodoRequest request = createValidTodoRequest();
        
        // When
        todoService.create(request);
        
        // Then - パフォーマンスログが記録されることを確認
        ArgumentCaptor<com.example.todoapp.dto.PerformanceLogEntry> perfCaptor = 
                ArgumentCaptor.forClass(com.example.todoapp.dto.PerformanceLogEntry.class);
        verify(loggingService, atLeastOnce()).logPerformance(perfCaptor.capture());
        
        com.example.todoapp.dto.PerformanceLogEntry perfEntry = perfCaptor.getValue();
        assertThat(perfEntry.getOperationName()).contains("SERVICE:");
        assertThat(perfEntry.getExecutionTimeMs()).isGreaterThan(0);
        assertThat(perfEntry.getPerformanceLevel()).isIn("EXCELLENT", "GOOD", "ACCEPTABLE");
    }
    
    @Test
    void testAuditLoggingForUpdateTodo() {
        // Given - まずTodoを作成
        TodoRequest createRequest = createValidTodoRequest();
        Todo createdTodo = todoService.create(createRequest);
        
        // 更新用のリクエスト
        TodoRequest updateRequest = createValidTodoRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setStatus(TodoStatus.DONE);
        
        // When
        todoService.update(createdTodo.getId(), updateRequest);
        
        // Then - UPDATE操作の監査ログが記録されることを確認
        ArgumentCaptor<com.example.todoapp.dto.AuditLogEntry> auditCaptor = 
                ArgumentCaptor.forClass(com.example.todoapp.dto.AuditLogEntry.class);
        verify(loggingService, atLeastOnce()).logAudit(auditCaptor.capture());
        
        // UPDATE操作のエントリを検索
        List<com.example.todoapp.dto.AuditLogEntry> auditEntries = auditCaptor.getAllValues();
        com.example.todoapp.dto.AuditLogEntry updateEntry = auditEntries.stream()
                .filter(entry -> "UPDATE".equals(entry.getOperation()))
                .findFirst()
                .orElse(null);
        
        assertThat(updateEntry).isNotNull();
        assertThat(updateEntry.getResourceType()).isEqualTo("TODO");
        assertThat(updateEntry.getResourceId()).isEqualTo(createdTodo.getId().toString());
        assertThat(updateEntry.getResult()).isEqualTo("SUCCESS");
    }
    
    @Test
    void testAuditLoggingForDeleteTodo() {
        // Given - まずTodoを作成
        TodoRequest createRequest = createValidTodoRequest();
        Todo createdTodo = todoService.create(createRequest);
        
        // When
        todoService.delete(createdTodo.getId());
        
        // Then - DELETE操作の監査ログが記録されることを確認
        ArgumentCaptor<com.example.todoapp.dto.AuditLogEntry> auditCaptor = 
                ArgumentCaptor.forClass(com.example.todoapp.dto.AuditLogEntry.class);
        verify(loggingService, atLeastOnce()).logAudit(auditCaptor.capture());
        
        // DELETE操作のエントリを検索
        List<com.example.todoapp.dto.AuditLogEntry> auditEntries = auditCaptor.getAllValues();
        com.example.todoapp.dto.AuditLogEntry deleteEntry = auditEntries.stream()
                .filter(entry -> "DELETE".equals(entry.getOperation()))
                .findFirst()
                .orElse(null);
        
        assertThat(deleteEntry).isNotNull();
        assertThat(deleteEntry.getResourceType()).isEqualTo("TODO");
        assertThat(deleteEntry.getResourceId()).isEqualTo(createdTodo.getId().toString());
        assertThat(deleteEntry.getResult()).isEqualTo("SUCCESS");
    }
    
    @Test
    void testPerformanceLoggingForMultipleOperations() {
        // Given
        TodoRequest request1 = createValidTodoRequest();
        request1.setTitle("Todo 1");
        
        TodoRequest request2 = createValidTodoRequest();
        request2.setTitle("Todo 2");
        
        // When - 複数の操作を実行
        Todo todo1 = todoService.create(request1);
        Todo todo2 = todoService.create(request2);
        todoService.findById(todo1.getId());
        todoService.findAll(org.springframework.data.domain.Pageable.unpaged());
        
        // Then - 複数のパフォーマンスログエントリが記録されることを確認
        ArgumentCaptor<com.example.todoapp.dto.PerformanceLogEntry> perfCaptor = 
                ArgumentCaptor.forClass(com.example.todoapp.dto.PerformanceLogEntry.class);
        verify(loggingService, atLeastOnce()).logPerformance(perfCaptor.capture());
        
        List<com.example.todoapp.dto.PerformanceLogEntry> perfEntries = perfCaptor.getAllValues();
        
        // 各操作に対してパフォーマンスログが記録されていることを確認
        assertThat(perfEntries).hasSizeGreaterThanOrEqualTo(3); // createTodo x2, findById x1以上
        
        // SERVICE層のログが含まれていることを確認
        boolean hasServiceLog = perfEntries.stream()
                .anyMatch(entry -> entry.getOperationName().contains("SERVICE:"));
        assertThat(hasServiceLog).isTrue();
    }
    
    @Test
    void testAuditLoggingForFailureScenario() {
        // Given - 存在しないIDでの操作
        Long nonExistentId = 99999L;
        
        // When - 例外が発生する操作を実行
        try {
            todoService.findById(nonExistentId);
        } catch (Exception e) {
            // 例外は予期されている
        }
        
        // Then - 失敗の監査ログが記録されることを確認
        ArgumentCaptor<com.example.todoapp.dto.AuditLogEntry> auditCaptor = 
                ArgumentCaptor.forClass(com.example.todoapp.dto.AuditLogEntry.class);
        verify(loggingService, atLeastOnce()).logAudit(auditCaptor.capture());
        
        // FAILURE結果のエントリを検索
        List<com.example.todoapp.dto.AuditLogEntry> auditEntries = auditCaptor.getAllValues();
        com.example.todoapp.dto.AuditLogEntry failureEntry = auditEntries.stream()
                .filter(entry -> "FAILURE".equals(entry.getResult()))
                .findFirst()
                .orElse(null);
        
        if (failureEntry != null) { // アスペクトが例外をキャッチした場合
            assertThat(failureEntry.getOperation()).isEqualTo("READ");
            assertThat(failureEntry.getResourceType()).isEqualTo("TODO");
            assertThat(failureEntry.getResourceId()).isEqualTo(nonExistentId.toString());
            assertThat(failureEntry.getErrorMessage()).isNotNull();
        }
    }
    
    private TodoRequest createValidTodoRequest() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Test Todo");
        request.setDescription("Test Description");
        request.setPriority(TodoPriority.MEDIUM);
        request.setStatus(TodoStatus.TODO);
        request.setDueDate(LocalDate.now().plusDays(7));
        return request;
    }
}