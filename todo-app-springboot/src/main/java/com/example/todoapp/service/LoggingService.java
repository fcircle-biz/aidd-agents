package com.example.todoapp.service;

import com.example.todoapp.dto.AuditLogEntry;
import com.example.todoapp.dto.PerformanceLogEntry;

/**
 * 構造化ログ記録のためのサービスインターフェース
 * アプリケーション全体でのログ記録の統一と管理を提供
 */
public interface LoggingService {
    
    /**
     * 監査ログを記録
     * ビジネス操作（CRUD等）の監査証跡を記録
     * 
     * @param entry 監査ログエントリ
     */
    void logAudit(AuditLogEntry entry);
    
    /**
     * パフォーマンスログを記録
     * メソッド実行時間やリソース使用量を記録
     * 
     * @param entry パフォーマンスログエントリ
     */
    void logPerformance(PerformanceLogEntry entry);
    
    /**
     * ビジネスオペレーションログを記録
     * アプリケーションの重要な業務処理の開始・完了を記録
     * 
     * @param operation 操作名
     * @param details 詳細情報
     */
    void logBusinessOperation(String operation, String details);
    
    /**
     * セキュリティ関連ログを記録
     * 認証・認可やセキュリティ脅威の検知を記録
     * 
     * @param event セキュリティイベント
     * @param details 詳細情報
     */
    void logSecurity(String event, String details);
    
    /**
     * API呼び出しログを記録
     * REST API呼び出しの詳細を記録
     * 
     * @param method HTTPメソッド
     * @param endpoint エンドポイント
     * @param statusCode ステータスコード
     * @param duration 処理時間（ミリ秒）
     */
    void logApiCall(String method, String endpoint, int statusCode, long duration);
    
    /**
     * データベース操作ログを記録
     * データベースの重要な操作を記録
     * 
     * @param operation 操作種別（SELECT, INSERT, UPDATE, DELETE）
     * @param table テーブル名
     * @param recordCount 対象レコード数
     */
    void logDatabaseOperation(String operation, String table, int recordCount);
    
    /**
     * 相関IDを設定
     * リクエストの追跡のための一意識別子を設定
     * 
     * @param correlationId 相関ID
     */
    void setCorrelationId(String correlationId);
    
    /**
     * ユーザーコンテキストを設定
     * 現在のユーザー情報をログコンテキストに設定
     * 
     * @param userId ユーザーID
     * @param userRole ユーザーロール
     */
    void setUserContext(String userId, String userRole);
    
    /**
     * ログコンテキストをクリア
     * リクエスト完了時にログコンテキストをクリーンアップ
     */
    void clearContext();
}