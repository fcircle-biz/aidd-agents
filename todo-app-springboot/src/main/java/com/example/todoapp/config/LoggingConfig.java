package com.example.todoapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * ログ設定クラス
 * アプリケーション全体のログ記録に関する設定を管理
 */
@Configuration
@EnableAspectJAutoProxy
public class LoggingConfig {
    
    /**
     * ログ記録用のObjectMapper
     * 監査ログやパフォーマンスログでのJSON変換に使用
     */
    @Bean
    public ObjectMapper loggingObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Java 8 時間API対応
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // null値の出力を抑制
        mapper.setDefaultPropertyInclusion(
                com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
        );
        
        // フォーマット設定
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        return mapper;
    }
}