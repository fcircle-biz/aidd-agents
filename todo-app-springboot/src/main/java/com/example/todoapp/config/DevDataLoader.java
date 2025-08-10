package com.example.todoapp.config;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Development environment data loader for seeding test data.
 * This component automatically loads sample data when running in development profile.
 */
@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DevDataLoader.class);

    @Autowired
    private TodoRepository todoRepository;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Loading development test data...");
        
        // Check if data already exists
        if (todoRepository.count() > 0) {
            logger.info("Development data already exists, skipping seed data creation");
            return;
        }
        
        loadSampleTodos();
        logger.info("Development test data loaded successfully");
    }

    private void loadSampleTodos() {
        List<Todo> sampleTodos = Arrays.asList(
            createTodo(
                "Spring Boot学習を完了する", 
                "Spring Bootの基本概念とベストプラクティスを学習し、実践的なスキルを身につける",
                TodoPriority.HIGH,
                TodoStatus.IN_PROGRESS,
                LocalDate.now().plusDays(7)
            ),
            createTodo(
                "データベース設計を確認する", 
                "H2データベースのスキーマ設計を見直し、パフォーマンス最適化を検討する",
                TodoPriority.MEDIUM,
                TodoStatus.TODO,
                LocalDate.now().plusDays(3)
            ),
            createTodo(
                "ユニットテストを追加する", 
                "サービス層とコントローラー層の包括的なテストケースを作成する",
                TodoPriority.HIGH,
                TodoStatus.TODO,
                LocalDate.now().plusDays(5)
            ),
            createTodo(
                "API ドキュメントを作成する", 
                "REST APIの詳細なドキュメントを作成し、使用例を追加する",
                TodoPriority.MEDIUM,
                TodoStatus.TODO,
                LocalDate.now().plusDays(10)
            ),
            createTodo(
                "ログイン機能の実装", 
                "Spring Securityを使用したユーザー認証・認可機能を実装する",
                TodoPriority.LOW,
                TodoStatus.TODO,
                LocalDate.now().plusDays(14)
            ),
            createTodo(
                "過去に完了したタスクのサンプル", 
                "開発環境での過去の作業例として追加されたサンプルタスク",
                TodoPriority.MEDIUM,
                TodoStatus.DONE,
                LocalDate.now().minusDays(2)
            ),
            createTodo(
                "緊急度の高いタスクのサンプル", 
                "高優先度タスクの表示テスト用データ",
                TodoPriority.HIGH,
                TodoStatus.IN_PROGRESS,
                LocalDate.now().plusDays(1)
            ),
            createTodo(
                "期限切れタスクのサンプル", 
                "期限切れタスクの表示とソート機能のテスト用データ",
                TodoPriority.MEDIUM,
                TodoStatus.TODO,
                LocalDate.now().minusDays(1)
            ),
            createTodo(
                "長期プロジェクトのサンプル", 
                "将来的な大規模プロジェクトの計画と管理方法を検討する。" +
                "このタスクは長期間にわたるプロジェクトの管理機能をテストするためのサンプルデータです。" +
                "複数の段階に分けて進行し、定期的な進捗確認とマイルストーンの設定が必要になります。",
                TodoPriority.LOW,
                TodoStatus.TODO,
                LocalDate.now().plusDays(30)
            ),
            createTodo(
                "パフォーマンス最適化", 
                "アプリケーションのパフォーマンス監視と最適化の実施",
                TodoPriority.MEDIUM,
                TodoStatus.IN_PROGRESS,
                LocalDate.now().plusDays(8)
            )
        );

        todoRepository.saveAll(sampleTodos);
        logger.info("Created {} sample todos for development", sampleTodos.size());
    }

    private Todo createTodo(String title, String description, TodoPriority priority, 
                           TodoStatus status, LocalDate dueDate) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority);
        todo.setStatus(status);
        todo.setDueDate(dueDate);
        todo.setCreatedAt(LocalDateTime.now().minusDays((int)(Math.random() * 5)));
        todo.setUpdatedAt(todo.getCreatedAt());
        return todo;
    }
}