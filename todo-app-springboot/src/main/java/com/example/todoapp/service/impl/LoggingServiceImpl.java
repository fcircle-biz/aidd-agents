package com.example.todoapp.service.impl;

import com.example.todoapp.dto.AuditLogEntry;
import com.example.todoapp.dto.PerformanceLogEntry;
import com.example.todoapp.service.LoggingService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * 構造化ログ記録サービスの実装
 * アプリケーション全体でのログ記録の統一と管理を提供
 */
@Service
@Slf4j
public class LoggingServiceImpl implements LoggingService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("audit");
    private static final Logger performanceLogger = LoggerFactory.getLogger("performance");
    
    // MDCキー定数
    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String USER_ROLE = "userRole";
    private static final String OPERATION = "operation";
    private static final String RESOURCE_TYPE = "resourceType";
    private static final String RESOURCE_ID = "resourceId";
    private static final String RESULT = "result";
    
    @Override
    public void logAudit(AuditLogEntry entry) {
        try {
            // MDCに監査情報を設定
            setMdcForAudit(entry);
            
            // 監査ログを記録
            auditLogger.info(entry.toLogMessage());
            
            // 失敗の場合は通常ログにも警告として記録
            if ("FAILURE".equals(entry.getResult())) {
                log.warn("Audit: Operation failed - {}", entry.toLogMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to log audit entry", e);
        } finally {
            clearAuditMdc();
        }
    }
    
    @Override
    public void logPerformance(PerformanceLogEntry entry) {
        try {
            // パフォーマンスログを記録
            performanceLogger.info(entry.toLogMessage());
            
            // 重要なパフォーマンス問題は通常ログにも記録
            if ("CRITICAL".equals(entry.getPerformanceLevel()) || "POOR".equals(entry.getPerformanceLevel())) {
                log.warn("Performance Issue: {}", entry.toLogMessage());
            }
            
        } catch (Exception e) {
            log.error("Failed to log performance entry", e);
        }
    }
    
    @Override
    public void logBusinessOperation(String operation, String details) {
        log.info("Business Operation: {} - {}", operation, details);
    }
    
    @Override
    public void logSecurity(String event, String details) {
        log.warn("Security Event: {} - {}", event, details);
    }
    
    @Override
    public void logApiCall(String method, String endpoint, int statusCode, long duration) {
        String level = determineApiLogLevel(statusCode);
        String message = String.format("API Call: %s %s - Status: %d, Duration: %dms", 
                                      method, endpoint, statusCode, duration);
        
        switch (level) {
            case "ERROR" -> log.error(message);
            case "WARN" -> log.warn(message);
            case "INFO" -> log.info(message);
            default -> log.debug(message);
        }
        
        // パフォーマンスが悪い場合はパフォーマンスログにも記録
        if (duration > 1000) {
            PerformanceLogEntry perfEntry = PerformanceLogEntry.builder()
                    .operationName("API_CALL")
                    .executionTimeMs(duration)
                    .additionalMetrics(java.util.Map.of(
                            "method", method,
                            "endpoint", endpoint,
                            "statusCode", statusCode
                    ))
                    .correlationId(MDC.get(CORRELATION_ID))
                    .userId(MDC.get(USER_ID))
                    .build();
            perfEntry.setPerformanceLevel(perfEntry.evaluatePerformanceLevel());
            logPerformance(perfEntry);
        }
    }
    
    @Override
    public void logDatabaseOperation(String operation, String table, int recordCount) {
        log.debug("Database Operation: {} on {} - {} records", operation, table, recordCount);
        
        // 大量データの処理は監視対象
        if (recordCount > 1000) {
            log.warn("Large Database Operation: {} on {} - {} records", operation, table, recordCount);
        }
    }
    
    @Override
    public void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            MDC.put(CORRELATION_ID, correlationId);
        }
    }
    
    @Override
    public void setUserContext(String userId, String userRole) {
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }
        if (userRole != null) {
            MDC.put(USER_ROLE, userRole);
        }
    }
    
    @Override
    public void clearContext() {
        MDC.clear();
    }
    
    /**
     * 監査ログ用のMDC設定
     */
    private void setMdcForAudit(AuditLogEntry entry) {
        if (entry.getUserId() != null) {
            MDC.put(USER_ID, entry.getUserId());
        }
        if (entry.getCorrelationId() != null) {
            MDC.put(CORRELATION_ID, entry.getCorrelationId());
        }
        if (entry.getOperation() != null) {
            MDC.put(OPERATION, entry.getOperation());
        }
        if (entry.getResourceType() != null) {
            MDC.put(RESOURCE_TYPE, entry.getResourceType());
        }
        if (entry.getResourceId() != null) {
            MDC.put(RESOURCE_ID, entry.getResourceId());
        }
        if (entry.getResult() != null) {
            MDC.put(RESULT, entry.getResult());
        }
    }
    
    /**
     * 監査用MDCクリア
     */
    private void clearAuditMdc() {
        MDC.remove(OPERATION);
        MDC.remove(RESOURCE_TYPE);
        MDC.remove(RESOURCE_ID);
        MDC.remove(RESULT);
    }
    
    /**
     * APIログレベル判定
     */
    private String determineApiLogLevel(int statusCode) {
        if (statusCode >= 500) {
            return "ERROR";
        } else if (statusCode >= 400) {
            return "WARN";
        } else if (statusCode >= 200 && statusCode < 300) {
            return "INFO";
        } else {
            return "DEBUG";
        }
    }
    
    /**
     * 現在のMDC情報を取得（デバッグ用）
     */
    public void logCurrentMdcContext() {
        log.debug("Current MDC Context: {}", MDC.getCopyOfContextMap());
    }
}