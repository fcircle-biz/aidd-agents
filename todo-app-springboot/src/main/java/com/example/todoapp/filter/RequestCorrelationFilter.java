package com.example.todoapp.filter;

import com.example.todoapp.service.LoggingService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * リクエスト相関IDフィルター
 * 各HTTPリクエストに対して一意の相関IDを生成し、ログコンテキストに設定
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RequestCorrelationFilter implements Filter {
    
    private final LoggingService loggingService;
    
    // ヘッダー名定数
    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
    private static final String USER_ID_HEADER_NAME = "X-User-ID";
    private static final String SESSION_ID_HEADER_NAME = "X-Session-ID";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 相関IDの設定
            String correlationId = extractOrGenerateCorrelationId(httpRequest);
            setupCorrelationContext(httpRequest, correlationId);
            
            // レスポンスヘッダーに相関IDを設定
            httpResponse.setHeader(CORRELATION_ID_HEADER_NAME, correlationId);
            
            // リクエスト開始ログ
            logRequestStart(httpRequest, correlationId);
            
            // 次のフィルターまたはサーブレットを実行
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            // エラー時のログ記録
            logRequestError(httpRequest, e);
            throw e;
            
        } finally {
            // リクエスト完了ログ
            long duration = System.currentTimeMillis() - startTime;
            logRequestEnd(httpRequest, httpResponse, duration);
            
            // ログコンテキストのクリーンアップ
            loggingService.clearContext();
        }
    }
    
    /**
     * 相関IDを抽出または生成
     */
    private String extractOrGenerateCorrelationId(HttpServletRequest request) {
        // リクエストヘッダーから相関IDを取得
        String correlationId = request.getHeader(CORRELATION_ID_HEADER_NAME);
        
        if (correlationId == null || correlationId.trim().isEmpty()) {
            // 相関IDがない場合は新規生成
            correlationId = generateCorrelationId();
        }
        
        // 相関IDの長さ制限と検証
        if (correlationId.length() > 64) {
            correlationId = correlationId.substring(0, 64);
        }
        
        return correlationId;
    }
    
    /**
     * 相関コンテキストの設定
     */
    private void setupCorrelationContext(HttpServletRequest request, String correlationId) {
        // 相関IDを設定
        loggingService.setCorrelationId(correlationId);
        
        // ユーザーコンテキストの設定（ヘッダーから取得）
        String userId = request.getHeader(USER_ID_HEADER_NAME);
        if (userId != null && !userId.trim().isEmpty()) {
            loggingService.setUserContext(userId, null);
        }
        
        // 追加のコンテキスト情報をMDCに設定
        MDC.put("requestMethod", request.getMethod());
        MDC.put("requestURI", request.getRequestURI());
        MDC.put("remoteAddr", getClientIpAddress(request));
        MDC.put("userAgent", request.getHeader("User-Agent"));
        
        String sessionId = request.getHeader(SESSION_ID_HEADER_NAME);
        if (sessionId != null) {
            MDC.put("sessionId", sessionId);
        }
    }
    
    /**
     * リクエスト開始ログ
     */
    private void logRequestStart(HttpServletRequest request, String correlationId) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String clientIp = getClientIpAddress(request);
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Request started: ").append(method).append(" ").append(uri);
        
        if (queryString != null && !queryString.isEmpty()) {
            logMessage.append("?").append(queryString);
        }
        
        logMessage.append(" from ").append(clientIp);
        
        log.info(logMessage.toString());
        
        // API呼び出しログ（開始時点）
        loggingService.logBusinessOperation("HTTP_REQUEST_START", 
                String.format("%s %s from %s", method, uri, clientIp));
    }
    
    /**
     * リクエスト完了ログ
     */
    private void logRequestEnd(HttpServletRequest request, HttpServletResponse response, long duration) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int statusCode = response.getStatus();
        
        log.info("Request completed: {} {} - Status: {}, Duration: {}ms", 
                method, uri, statusCode, duration);
        
        // API呼び出しログ
        loggingService.logApiCall(method, uri, statusCode, duration);
    }
    
    /**
     * リクエストエラーログ
     */
    private void logRequestError(HttpServletRequest request, Exception e) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = getClientIpAddress(request);
        
        log.error("Request error: {} {} from {} - Error: {}", 
                method, uri, clientIp, e.getMessage(), e);
        
        // セキュリティログ（エラーの種類によって）
        if (isSecurityRelevantError(e)) {
            loggingService.logSecurity("REQUEST_ERROR", 
                    String.format("Error in %s %s from %s: %s", method, uri, clientIp, e.getMessage()));
        }
    }
    
    /**
     * 相関IDの生成
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * クライアントIPアドレスの取得
     * プロキシやロードバランサーを考慮した実装
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * セキュリティ関連エラーかどうかの判定
     */
    private boolean isSecurityRelevantError(Exception e) {
        String errorType = e.getClass().getSimpleName().toLowerCase();
        
        return errorType.contains("security") || 
               errorType.contains("authentication") || 
               errorType.contains("authorization") ||
               errorType.contains("forbidden") ||
               errorType.contains("unauthorized");
    }
}