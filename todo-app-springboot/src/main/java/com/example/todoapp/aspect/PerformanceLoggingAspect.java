package com.example.todoapp.aspect;

import com.example.todoapp.dto.PerformanceLogEntry;
import com.example.todoapp.service.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * パフォーマンス監視アスペクト
 * メソッド実行時間とリソース使用量を自動的に記録
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceLoggingAspect {
    
    private final LoggingService loggingService;
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    
    /**
     * サービス層のメソッド実行時間を監視（LoggingServiceImplは除外）
     */
    @Around("execution(* com.example.todoapp.service..*.*(..)) && " +
            "!execution(* com.example.todoapp.service.impl.LoggingServiceImpl.*(..))")
    public Object logServiceMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodPerformance(joinPoint, "SERVICE");
    }
    
    /**
     * コントローラーのメソッド実行時間を監視
     */
    @Around("execution(* com.example.todoapp.controller..*.*(..))")
    public Object logControllerMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodPerformance(joinPoint, "CONTROLLER");
    }
    
    /**
     * リポジトリの重要なメソッド実行時間を監視
     */
    @Around("execution(* com.example.todoapp.repository..*.*(..)) && " +
            "!execution(* com.example.todoapp.repository..*.findById(..))")
    public Object logRepositoryMethodPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodPerformance(joinPoint, "REPOSITORY");
    }
    
    /**
     * メソッドのパフォーマンスを監視する共通メソッド
     */
    private Object logMethodPerformance(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String operationName = layer + ":" + className + "." + methodName;
        
        long startTime = System.currentTimeMillis();
        long startCpuTime = getCurrentThreadCpuTime();
        long startMemory = getUsedMemory();
        
        Object result = null;
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
            
        } catch (Throwable throwable) {
            errorMessage = throwable.getMessage();
            throw throwable;
            
        } finally {
            long endTime = System.currentTimeMillis();
            long endCpuTime = getCurrentThreadCpuTime();
            long endMemory = getUsedMemory();
            
            long executionTime = endTime - startTime;
            Long cpuTime = (endCpuTime > 0 && startCpuTime > 0) ? endCpuTime - startCpuTime : null;
            Long memoryDiff = endMemory - startMemory;
            
            // パフォーマンスログエントリを作成
            PerformanceLogEntry.PerformanceLogEntryBuilder entryBuilder = PerformanceLogEntry.builder()
                    .operationName(operationName)
                    .className(className)
                    .methodName(methodName)
                    .executionTimeMs(executionTime)
                    .cpuTimeNanos(cpuTime)
                    .correlationId(getCurrentCorrelationId())
                    .userId(getCurrentUserId())
                    .errorMessage(errorMessage);
            
            // メモリ使用量が有効な場合のみ設定
            if (memoryDiff > 0) {
                entryBuilder.memoryUsedBytes(memoryDiff);
            }
            
            // 結果の記録数を設定
            Integer recordCount = extractRecordCount(result);
            if (recordCount != null) {
                entryBuilder.recordCount(recordCount);
            }
            
            // パラメータ情報の追加（開発環境のみ）
            if (isDebugEnabled()) {
                Map<String, Object> parameters = extractParameters(joinPoint);
                entryBuilder.parameters(parameters);
            }
            
            // 追加メトリクスの設定
            Map<String, Object> additionalMetrics = createAdditionalMetrics(layer, result);
            entryBuilder.additionalMetrics(additionalMetrics);
            
            PerformanceLogEntry entry = entryBuilder.build();
            entry.setPerformanceLevel(entry.evaluatePerformanceLevel());
            
            // しきい値を超える場合のみログ記録（デバッグモード以外）
            if (shouldLogPerformance(executionTime, entry.getPerformanceLevel(), errorMessage != null)) {
                loggingService.logPerformance(entry);
            }
        }
    }
    
    /**
     * 結果からレコード数を抽出
     */
    private Integer extractRecordCount(Object result) {
        if (result instanceof List<?> list) {
            return list.size();
        } else if (result instanceof Page<?> page) {
            return (int) page.getTotalElements();
        }
        return null;
    }
    
    /**
     * パラメータ情報を抽出（開発用）
     */
    private Map<String, Object> extractParameters(ProceedingJoinPoint joinPoint) {
        Map<String, Object> parameters = new HashMap<>();
        Object[] args = joinPoint.getArgs();
        
        if (args != null && args.length > 0) {
            for (int i = 0; i < Math.min(args.length, 3); i++) { // 最大3つまで
                Object arg = args[i];
                if (arg != null) {
                    parameters.put("arg" + i, arg.getClass().getSimpleName());
                }
            }
        }
        
        return parameters;
    }
    
    /**
     * 追加メトリクスを作成
     */
    private Map<String, Object> createAdditionalMetrics(String layer, Object result) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("layer", layer);
        
        if (result != null) {
            metrics.put("resultType", result.getClass().getSimpleName());
        }
        
        return metrics;
    }
    
    /**
     * パフォーマンスログを記録すべきかの判定
     */
    private boolean shouldLogPerformance(long executionTime, String performanceLevel, boolean hasError) {
        // エラーがある場合は常に記録
        if (hasError) {
            return true;
        }
        
        // デバッグモードでは全て記録
        if (isDebugEnabled()) {
            return true;
        }
        
        // パフォーマンスが悪い場合は記録
        if ("POOR".equals(performanceLevel) || "CRITICAL".equals(performanceLevel)) {
            return true;
        }
        
        // 長時間実行は記録
        if (executionTime > 500) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 現在のスレッドのCPU時間を取得
     */
    private long getCurrentThreadCpuTime() {
        try {
            if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
                return threadMXBean.getCurrentThreadCpuTime();
            }
        } catch (Exception e) {
            // CPU時間の取得に失敗した場合は無視
        }
        return -1;
    }
    
    /**
     * 使用中のメモリ量を取得
     */
    private long getUsedMemory() {
        try {
            return memoryMXBean.getHeapMemoryUsage().getUsed();
        } catch (Exception e) {
            // メモリ情報の取得に失敗した場合は無視
            return 0;
        }
    }
    
    /**
     * デバッグモードかどうかの判定
     */
    private boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }
    
    /**
     * 現在のユーザーIDを取得
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