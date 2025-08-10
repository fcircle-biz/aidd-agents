package com.example.todoapp.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

/**
 * 監査ログエントリ
 * ビジネス操作の監査証跡を記録するためのデータ構造
 */
@Data
@Builder
@Jacksonized
public class AuditLogEntry {
    
    /**
     * 操作種別（CREATE, READ, UPDATE, DELETE, SEARCH）
     */
    private String operation;
    
    /**
     * リソース種別（TODO, USER等）
     */
    private String resourceType;
    
    /**
     * リソースID
     */
    private String resourceId;
    
    /**
     * ユーザーID
     */
    private String userId;
    
    /**
     * 操作結果（SUCCESS, FAILURE）
     */
    private String result;
    
    /**
     * 詳細情報
     */
    private String details;
    
    /**
     * 操作前の値（更新・削除の場合）
     */
    private String oldValue;
    
    /**
     * 操作後の値（作成・更新の場合）
     */
    private String newValue;
    
    /**
     * IPアドレス
     */
    private String ipAddress;
    
    /**
     * ユーザーエージェント
     */
    private String userAgent;
    
    /**
     * セッションID
     */
    private String sessionId;
    
    /**
     * 相関ID
     */
    private String correlationId;
    
    /**
     * タイムスタンプ
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * 失敗時のエラー詳細
     */
    private String errorMessage;
    
    /**
     * 失敗時のエラーコード
     */
    private String errorCode;
    
    /**
     * 監査ログエントリの文字列表現を生成
     */
    public String toLogMessage() {
        StringBuilder message = new StringBuilder();
        message.append("User ").append(userId != null ? userId : "anonymous")
               .append(" performed ").append(operation)
               .append(" on ").append(resourceType);
               
        if (resourceId != null) {
            message.append(" (ID: ").append(resourceId).append(")");
        }
        
        message.append(" - Result: ").append(result);
        
        if (details != null) {
            message.append(" - Details: ").append(details);
        }
        
        if ("FAILURE".equals(result) && errorMessage != null) {
            message.append(" - Error: ").append(errorMessage);
        }
        
        return message.toString();
    }
    
    /**
     * 成功した操作のための簡易ファクトリメソッド
     */
    public static AuditLogEntry success(String operation, String resourceType, String resourceId, String userId) {
        return AuditLogEntry.builder()
                .operation(operation)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .userId(userId)
                .result("SUCCESS")
                .build();
    }
    
    /**
     * 失敗した操作のための簡易ファクトリメソッド
     */
    public static AuditLogEntry failure(String operation, String resourceType, String resourceId, String userId, 
                                       String errorMessage, String errorCode) {
        return AuditLogEntry.builder()
                .operation(operation)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .userId(userId)
                .result("FAILURE")
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }
}