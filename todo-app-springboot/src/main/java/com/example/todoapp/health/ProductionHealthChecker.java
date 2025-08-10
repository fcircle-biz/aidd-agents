package com.example.todoapp.health;

import com.example.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * プロダクション環境向けのヘルスチェッカー
 * 
 * データベース接続、リポジトリアクセス、基本的なアプリケーション機能の
 * ヘルスチェックを実行します。
 * 
 * @author System
 */
@Component("productionHealthChecker")
@Profile("prod")
public class ProductionHealthChecker {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TodoRepository todoRepository;

    public Map<String, Object> checkHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        
        try {
            // データベース接続チェック
            checkDatabaseConnection(healthInfo);
            
            // リポジトリアクセスチェック
            checkRepositoryAccess(healthInfo);
            
            // アプリケーション統計情報
            addApplicationStatistics(healthInfo);
            
            healthInfo.put("status", "Production Ready");
            healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            healthInfo.put("overallStatus", "UP");
                
        } catch (Exception e) {
            healthInfo.put("overallStatus", "DOWN");
            healthInfo.put("error", e.getMessage());
            healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        return healthInfo;
    }

    private void checkDatabaseConnection(Map<String, Object> healthInfo) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) { // 5秒でタイムアウト
                healthInfo.put("database", "Connected");
                healthInfo.put("databaseUrl", connection.getMetaData().getURL());
                healthInfo.put("databaseDriver", connection.getMetaData().getDriverName());
            } else {
                throw new SQLException("Database connection is not valid");
            }
        }
    }

    private void checkRepositoryAccess(Map<String, Object> healthInfo) {
        try {
            long todoCount = todoRepository.count();
            healthInfo.put("repository", "Accessible");
            healthInfo.put("repositoryTodoCount", todoCount);
        } catch (Exception e) {
            healthInfo.put("repository", "Error: " + e.getMessage());
            throw e;
        }
    }

    private void addApplicationStatistics(Map<String, Object> healthInfo) {
        try {
            // JVM統計情報
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;

            healthInfo.put("jvmMemoryMax", formatBytes(maxMemory));
            healthInfo.put("jvmMemoryTotal", formatBytes(totalMemory));
            healthInfo.put("jvmMemoryUsed", formatBytes(usedMemory));
            healthInfo.put("jvmMemoryFree", formatBytes(freeMemory));
            healthInfo.put("jvmMemoryUsage", String.format("%.2f%%", (double) usedMemory / totalMemory * 100));

            // システム統計情報
            healthInfo.put("systemProcessors", runtime.availableProcessors());
            healthInfo.put("systemUptime", System.currentTimeMillis() - getProcessStartTime());
            
        } catch (Exception e) {
            healthInfo.put("statistics", "Error: " + e.getMessage());
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private long getProcessStartTime() {
        // 簡易的な実装：JVM起動時間の推定
        return System.currentTimeMillis() - 
               ManagementFactory.getRuntimeMXBean().getUptime();
    }
}