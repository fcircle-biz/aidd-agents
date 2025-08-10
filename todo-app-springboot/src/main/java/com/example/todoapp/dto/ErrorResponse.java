package com.example.todoapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * エラーレスポンス用DTO
 * API呼び出し時のエラー情報を統一形式で返却するために使用
 */
public class ErrorResponse {
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private int status;
    
    private String error;
    
    private String message;
    
    private String path;
    
    private List<FieldError> errors;
    
    // デフォルトコンストラクター
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    // 基本コンストラクター
    public ErrorResponse(int status, String message) {
        this();
        this.status = status;
        this.message = message;
        this.error = getErrorName(status);
    }
    
    // 詳細コンストラクター
    public ErrorResponse(int status, String message, String path) {
        this(status, message);
        this.path = path;
    }
    
    // フィールドエラー付きコンストラクター
    public ErrorResponse(int status, String message, List<FieldError> errors) {
        this(status, message);
        this.errors = errors;
    }
    
    // HTTPステータスコードから対応するエラー名を取得
    private String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 409 -> "Conflict";
            case 422 -> "Unprocessable Entity";
            case 500 -> "Internal Server Error";
            case 503 -> "Service Unavailable";
            default -> "Unknown Error";
        };
    }
    
    // フィールドエラー内部クラス
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
        
        public FieldError() {}
        
        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }
        
        // Getter/Setter
        public String getField() {
            return field;
        }
        
        public void setField(String field) {
            this.field = field;
        }
        
        public Object getRejectedValue() {
            return rejectedValue;
        }
        
        public void setRejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    // Getter/Setter
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
        this.error = getErrorName(status);
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public List<FieldError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }
}