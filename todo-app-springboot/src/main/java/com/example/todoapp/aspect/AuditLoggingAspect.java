package com.example.todoapp.aspect;

import com.example.todoapp.dto.AuditLogEntry;
import com.example.todoapp.dto.TodoResponse;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.service.LoggingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 監査ログアスペクト
 * サービス層のCRUD操作を自動的に監査ログに記録
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggingAspect {
    
    private final LoggingService loggingService;
    private final ObjectMapper objectMapper;
    
    // ThreadLocalでメソッド実行前の状態を保持
    private final ThreadLocal<Object> beforeState = new ThreadLocal<>();
    
    /**
     * Todo作成操作の監査ログ
     */
    @AfterReturning(pointcut = "execution(* com.example.todoapp.service.*.create(..))", returning = "result")
    public void logCreate(JoinPoint joinPoint, Object result) {
        try {
            String resourceId = extractResourceId(result);
            String newValue = serializeObject(result);
            
            AuditLogEntry entry = AuditLogEntry.builder()
                    .operation("CREATE")
                    .resourceType("TODO")
                    .resourceId(resourceId)
                    .userId(getCurrentUserId())
                    .result("SUCCESS")
                    .newValue(newValue)
                    .details("Todo created successfully")
                    .correlationId(getCurrentCorrelationId())
                    .build();
                    
            loggingService.logAudit(entry);
            
        } catch (Exception e) {
            log.error("Failed to log audit for create", e);
        }
    }
    
    /**
     * Todo読み取り操作の監査ログ（詳細取得）
     */
    @AfterReturning(pointcut = "execution(* com.example.todoapp.service.*.findById(..))", returning = "result")
    public void logFindTodo(JoinPoint joinPoint, Object result) {
        try {
            String resourceId = extractIdFromArgs(joinPoint.getArgs());
            
            AuditLogEntry entry = AuditLogEntry.builder()
                    .operation("READ")
                    .resourceType("TODO")
                    .resourceId(resourceId)
                    .userId(getCurrentUserId())
                    .result("SUCCESS")
                    .details("Todo retrieved successfully")
                    .correlationId(getCurrentCorrelationId())
                    .build();
                    
            loggingService.logAudit(entry);
            
        } catch (Exception e) {
            log.error("Failed to log audit for findById", e);
        }
    }
    
    /**
     * Todo更新操作の監査ログ（更新前の状態を記録）
     */
    @Before("execution(* com.example.todoapp.service.*.update(..))")
    public void beforeUpdate(JoinPoint joinPoint) {
        try {
            // 更新前の状態を取得して保存
            String todoId = extractIdFromArgs(joinPoint.getArgs());
            if (todoId != null) {
                // 注意: 実際の実装では、ここでサービスから既存のTodoを取得する必要がある
                // 簡略化のため、IDのみを保存
                beforeState.set(todoId);
            }
        } catch (Exception e) {
            log.error("Failed to capture before state for update", e);
        }
    }
    
    /**
     * Todo更新操作の監査ログ（更新後）
     */
    @AfterReturning(pointcut = "execution(* com.example.todoapp.service.*.update(..))", returning = "result")
    public void logUpdate(JoinPoint joinPoint, Object result) {
        try {
            String resourceId = extractResourceId(result);
            String oldValue = beforeState.get() != null ? "ID: " + beforeState.get() : null;
            String newValue = serializeObject(result);
            
            AuditLogEntry entry = AuditLogEntry.builder()
                    .operation("UPDATE")
                    .resourceType("TODO")
                    .resourceId(resourceId)
                    .userId(getCurrentUserId())
                    .result("SUCCESS")
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .details("Todo updated successfully")
                    .correlationId(getCurrentCorrelationId())
                    .build();
                    
            loggingService.logAudit(entry);
            
        } catch (Exception e) {
            log.error("Failed to log audit for update", e);
        } finally {
            beforeState.remove();
        }
    }
    
    /**
     * Todo削除操作の監査ログ
     */
    @AfterReturning(pointcut = "execution(* com.example.todoapp.service.*.delete(..))")
    public void logDelete(JoinPoint joinPoint) {
        try {
            String resourceId = extractIdFromArgs(joinPoint.getArgs());
            
            AuditLogEntry entry = AuditLogEntry.builder()
                    .operation("DELETE")
                    .resourceType("TODO")
                    .resourceId(resourceId)
                    .userId(getCurrentUserId())
                    .result("SUCCESS")
                    .details("Todo deleted successfully")
                    .correlationId(getCurrentCorrelationId())
                    .build();
                    
            loggingService.logAudit(entry);
            
        } catch (Exception e) {
            log.error("Failed to log audit for delete", e);
        }
    }
    
    /**
     * Todo検索操作の監査ログ
     */
    @AfterReturning(pointcut = "execution(* com.example.todoapp.service.*.search(..))", returning = "result")
    public void logSearch(JoinPoint joinPoint, Object result) {
        try {
            int resultCount = 0;
            if (result instanceof List<?> list) {
                resultCount = list.size();
            } else if (result instanceof Page<?> page) {
                resultCount = (int) page.getTotalElements();
            }
            
            AuditLogEntry entry = AuditLogEntry.builder()
                    .operation("SEARCH")
                    .resourceType("TODO")
                    .userId(getCurrentUserId())
                    .result("SUCCESS")
                    .details("Todo search completed - " + resultCount + " results found")
                    .correlationId(getCurrentCorrelationId())
                    .build();
                    
            loggingService.logAudit(entry);
            
        } catch (Exception e) {
            log.error("Failed to log audit for search", e);
        }
    }
    
    /**
     * 例外発生時の監査ログ
     */
    @AfterThrowing(pointcut = "execution(* com.example.todoapp.service.*.*(..)) && " +
                              "(execution(* *..create(..)) || " +
                              "execution(* *..update(..)) || " +
                              "execution(* *..delete(..)) || " +
                              "execution(* *..findById(..))", throwing = "exception")
    public void logOperationFailure(JoinPoint joinPoint, Throwable exception) {
        try {
            String operation = determineOperation(joinPoint.getSignature().getName());
            String resourceId = extractIdFromArgs(joinPoint.getArgs());
            
            AuditLogEntry entry = AuditLogEntry.builder()
                    .operation(operation)
                    .resourceType("TODO")
                    .resourceId(resourceId)
                    .userId(getCurrentUserId())
                    .result("FAILURE")
                    .errorMessage(exception.getMessage())
                    .errorCode(exception.getClass().getSimpleName())
                    .details("Operation failed with exception")
                    .correlationId(getCurrentCorrelationId())
                    .build();
                    
            loggingService.logAudit(entry);
            
        } catch (Exception e) {
            log.error("Failed to log audit for operation failure", e);
        } finally {
            beforeState.remove();
        }
    }
    
    /**
     * リソースIDをレスポンスオブジェクトから抽出
     */
    private String extractResourceId(Object result) {
        if (result instanceof TodoResponse todoResponse) {
            return todoResponse.getId() != null ? todoResponse.getId().toString() : null;
        } else if (result instanceof Todo todo) {
            return todo.getId() != null ? todo.getId().toString() : null;
        }
        return null;
    }
    
    /**
     * 引数からIDを抽出
     */
    private String extractIdFromArgs(Object[] args) {
        if (args != null && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof Long longId) {
                return longId.toString();
            } else if (firstArg instanceof String strId) {
                return strId;
            }
        }
        return null;
    }
    
    /**
     * オブジェクトをJSON文字列にシリアライズ
     */
    private String serializeObject(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj != null ? obj.toString() : "null";
        }
    }
    
    /**
     * メソッド名から操作種別を判定
     */
    private String determineOperation(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("find") || methodName.startsWith("get")) return "READ";
        if (methodName.startsWith("search")) return "SEARCH";
        return "UNKNOWN";
    }
    
    /**
     * 現在のユーザーIDを取得（実際の実装では認証コンテキストから取得）
     * 現在は簡略化のため固定値
     */
    private String getCurrentUserId() {
        // TODO: Spring Securityの認証コンテキストから実際のユーザーIDを取得
        return "system"; // 暫定値
    }
    
    /**
     * 現在の相関IDを取得
     */
    private String getCurrentCorrelationId() {
        return org.slf4j.MDC.get("correlationId");
    }
}