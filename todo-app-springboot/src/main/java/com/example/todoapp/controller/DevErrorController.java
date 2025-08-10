package com.example.todoapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Development-specific error controller.
 * Provides detailed error information and debugging tools for development environment.
 */
@Controller
@Profile("dev")
public class DevErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(DevErrorController.class);
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error attributes
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // Log the error for development
        logger.error("Development error occurred - Status: {}, URI: {}, Message: {}", 
                    status, requestUri, errorMessage, (Throwable) exception);
        
        // Add attributes to model for the error page
        model.addAttribute("status", status != null ? status : "Unknown");
        model.addAttribute("error", getErrorType((Integer) status));
        model.addAttribute("message", errorMessage != null ? errorMessage : "予期しないエラーが発生しました");
        model.addAttribute("path", requestUri != null ? requestUri : "Unknown");
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Add exception details if available
        if (exception != null && exception instanceof Throwable) {
            Throwable throwable = (Throwable) exception;
            model.addAttribute("exception", throwable);
            
            // Create stack trace string
            StringBuilder stackTrace = new StringBuilder();
            stackTrace.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");
            
            for (StackTraceElement element : throwable.getStackTrace()) {
                stackTrace.append("\tat ").append(element.toString()).append("\n");
                
                // Limit stack trace to prevent extremely long output
                if (stackTrace.length() > 5000) {
                    stackTrace.append("\t... (truncated for display)");
                    break;
                }
            }
            
            // Add caused by information
            Throwable cause = throwable.getCause();
            if (cause != null && cause != throwable) {
                stackTrace.append("Caused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage()).append("\n");
                for (int i = 0; i < Math.min(5, cause.getStackTrace().length); i++) {
                    stackTrace.append("\tat ").append(cause.getStackTrace()[i].toString()).append("\n");
                }
            }
            
            model.addAttribute("trace", stackTrace.toString());
        }
        
        // Add request information for debugging
        model.addAttribute("method", request.getMethod());
        model.addAttribute("url", request.getRequestURL().toString());
        model.addAttribute("userAgent", request.getHeader("User-Agent"));
        model.addAttribute("clientIP", getClientIP(request));
        
        return "error/dev-error";
    }
    
    private String getErrorType(Integer status) {
        if (status == null) return "Unknown Error";
        
        switch (status) {
            case 400: return "Bad Request";
            case 401: return "Unauthorized";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 409: return "Conflict";
            case 422: return "Unprocessable Entity";
            case 500: return "Internal Server Error";
            case 501: return "Not Implemented";
            case 502: return "Bad Gateway";
            case 503: return "Service Unavailable";
            case 504: return "Gateway Timeout";
            default: return "HTTP Error " + status;
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}