package com.example.todoapp.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * データベース設定クラス
 * 
 * JPAとHibernateの設定を行う
 * 
 * @author System
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.example.todoapp.repository")
@EntityScan(basePackages = "com.example.todoapp.entity")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // データベース設定は主にapplication.propertiesで行う
    // 必要に応じてカスタム設定を追加
}