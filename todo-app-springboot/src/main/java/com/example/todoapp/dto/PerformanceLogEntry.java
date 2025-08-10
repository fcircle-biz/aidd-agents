package com.example.todoapp.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * パフォーマンスログエントリ
 * メソッド実行時間やリソース使用量を記録するためのデータ構造
 */
@Data
@Builder
@Jacksonized
public class PerformanceLogEntry {
    
    /**
     * 操作名またはメソッド名
     */
    private String operationName;
    
    /**
     * クラス名
     */
    private String className;
    
    /**
     * メソッド名
     */
    private String methodName;
    
    /**
     * 実行時間（ミリ秒）
     */
    private long executionTimeMs;
    
    /**
     * CPU時間（ナノ秒）
     */
    private Long cpuTimeNanos;
    
    /**
     * メモリ使用量（バイト）
     */
    private Long memoryUsedBytes;
    
    /**
     * データベースアクセス回数
     */
    private Integer dbAccessCount;
    
    /**
     * 処理されたレコード数
     */
    private Integer recordCount;
    
    /**
     * パラメータ情報
     */
    private Map<String, Object> parameters;
    
    /**
     * 戻り値情報
     */
    private String returnValue;
    
    /**
     * 相関ID
     */
    private String correlationId;
    
    /**
     * ユーザーID
     */
    private String userId;
    
    /**
     * タイムスタンプ
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * エラー情報（例外発生時）
     */
    private String errorMessage;
    
    /**
     * 追加のメトリクス
     */
    private Map<String, Object> additionalMetrics;
    
    /**
     * パフォーマンス評価レベル
     * EXCELLENT, GOOD, ACCEPTABLE, POOR, CRITICAL
     */
    private String performanceLevel;
    
    /**
     * パフォーマンスログエントリの文字列表現を生成
     */
    public String toLogMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Performance: ");
        
        if (className != null && methodName != null) {
            message.append(className).append(".").append(methodName);
        } else if (operationName != null) {
            message.append(operationName);
        }
        
        message.append(" executed in ").append(executionTimeMs).append("ms");
        
        if (recordCount != null) {
            message.append(" (").append(recordCount).append(" records)");
        }
        
        if (performanceLevel != null) {
            message.append(" - Level: ").append(performanceLevel);
        }
        
        if (dbAccessCount != null) {
            message.append(" - DB Access: ").append(dbAccessCount);
        }
        
        if (memoryUsedBytes != null) {
            message.append(" - Memory: ").append(formatBytes(memoryUsedBytes));
        }
        
        if (errorMessage != null) {
            message.append(" - Error: ").append(errorMessage);
        }
        
        return message.toString();
    }
    
    /**
     * バイト数を人間が読みやすい形式にフォーマット
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + "B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f%sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * 実行時間に基づいてパフォーマンスレベルを自動判定
     */
    public String evaluatePerformanceLevel() {
        if (executionTimeMs < 100) {
            return "EXCELLENT";
        } else if (executionTimeMs < 500) {
            return "GOOD";
        } else if (executionTimeMs < 2000) {
            return "ACCEPTABLE";
        } else if (executionTimeMs < 10000) {
            return "POOR";
        } else {
            return "CRITICAL";
        }
    }
    
    /**
     * 簡易ファクトリメソッド
     */
    public static PerformanceLogEntry of(String operationName, long executionTimeMs) {
        PerformanceLogEntry entry = PerformanceLogEntry.builder()
                .operationName(operationName)
                .executionTimeMs(executionTimeMs)
                .build();
        entry.setPerformanceLevel(entry.evaluatePerformanceLevel());
        return entry;
    }
    
    /**
     * メソッド用ファクトリメソッド
     */
    public static PerformanceLogEntry forMethod(String className, String methodName, long executionTimeMs) {
        PerformanceLogEntry entry = PerformanceLogEntry.builder()
                .className(className)
                .methodName(methodName)
                .operationName(className + "." + methodName)
                .executionTimeMs(executionTimeMs)
                .build();
        entry.setPerformanceLevel(entry.evaluatePerformanceLevel());
        return entry;
    }
}