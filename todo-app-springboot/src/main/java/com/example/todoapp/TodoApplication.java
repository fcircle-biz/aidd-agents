package com.example.todoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Todo管理アプリケーションのメインクラス
 * 
 * Spring Bootアプリケーションのエントリーポイント
 * H2組み込みデータベースを使用したTodo管理システム
 */
@SpringBootApplication
public class TodoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoApplication.class, args);
    }
}