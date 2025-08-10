package com.example.todoapp.controller;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.repository.TodoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Development data management controller.
 * Provides endpoints for managing test data in development environment.
 */
@RestController
@RequestMapping("/dev/data")
@Profile("dev")
public class DevDataController {

    private static final Logger logger = LoggerFactory.getLogger(DevDataController.class);

    @Autowired
    private TodoRepository todoRepository;

    /**
     * Get current database statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDataStatistics() {
        logger.debug("Retrieving database statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTodos", todoRepository.count());
        stats.put("completedTodos", todoRepository.countByStatus(TodoStatus.COMPLETED));
        stats.put("pendingTodos", todoRepository.countByStatus(TodoStatus.PENDING));
        stats.put("inProgressTodos", todoRepository.countByStatus(TodoStatus.IN_PROGRESS));
        stats.put("highPriorityTodos", todoRepository.countByPriority(TodoPriority.HIGH));
        stats.put("mediumPriorityTodos", todoRepository.countByPriority(TodoPriority.MEDIUM));
        stats.put("lowPriorityTodos", todoRepository.countByPriority(TodoPriority.LOW));
        stats.put("overdueTodos", todoRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), TodoStatus.COMPLETED));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Reset all test data
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetTestData() {
        logger.info("Resetting test data");
        
        long deletedCount = todoRepository.count();
        todoRepository.deleteAll();
        
        // Reload sample data
        loadSampleTodos();
        long newCount = todoRepository.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Test data reset successfully");
        result.put("deletedCount", deletedCount);
        result.put("newCount", newCount);
        
        logger.info("Test data reset completed. Deleted: {}, Created: {}", deletedCount, newCount);
        return ResponseEntity.ok(result);
    }

    /**
     * Add additional test data
     */
    @PostMapping("/seed/{count}")
    public ResponseEntity<Map<String, Object>> addTestData(@PathVariable int count) {
        logger.info("Adding {} additional test todos", count);
        
        if (count <= 0 || count > 100) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Count must be between 1 and 100");
            return ResponseEntity.badRequest().body(error);
        }
        
        long beforeCount = todoRepository.count();
        
        for (int i = 1; i <= count; i++) {
            Todo todo = createRandomTodo(i);
            todoRepository.save(todo);
        }
        
        long afterCount = todoRepository.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Test data added successfully");
        result.put("beforeCount", beforeCount);
        result.put("afterCount", afterCount);
        result.put("addedCount", afterCount - beforeCount);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Clear all data
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        logger.info("Clearing all test data");
        
        long deletedCount = todoRepository.count();
        todoRepository.deleteAll();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "All data cleared successfully");
        result.put("deletedCount", deletedCount);
        
        logger.info("All test data cleared. Deleted: {} records", deletedCount);
        return ResponseEntity.ok(result);
    }

    /**
     * Create overdue todos for testing
     */
    @PostMapping("/create-overdue/{count}")
    public ResponseEntity<Map<String, Object>> createOverdueTodos(@PathVariable int count) {
        logger.info("Creating {} overdue todos for testing", count);
        
        if (count <= 0 || count > 50) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Count must be between 1 and 50");
            return ResponseEntity.badRequest().body(error);
        }
        
        for (int i = 1; i <= count; i++) {
            Todo todo = new Todo();
            todo.setTitle("期限切れタスク " + i);
            todo.setDescription("テスト用の期限切れタスクです。期限切れ機能の動作確認に使用されます。");
            todo.setPriority(TodoPriority.values()[(int)(Math.random() * TodoPriority.values().length)]);
            todo.setStatus(TodoStatus.PENDING);
            todo.setDueDate(LocalDate.now().minusDays((int)(Math.random() * 10) + 1));
            todo.setCreatedAt(LocalDateTime.now().minusDays((int)(Math.random() * 15)));
            todo.setUpdatedAt(todo.getCreatedAt());
            
            todoRepository.save(todo);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Overdue todos created successfully");
        result.put("createdCount", count);
        
        return ResponseEntity.ok(result);
    }

    private void loadSampleTodos() {
        List<Todo> sampleTodos = Arrays.asList(
            createTodo("開発環境テスト1", "開発環境での基本機能テスト", TodoPriority.MEDIUM, TodoStatus.PENDING, LocalDate.now().plusDays(3)),
            createTodo("開発環境テスト2", "API動作確認テスト", TodoPriority.HIGH, TodoStatus.IN_PROGRESS, LocalDate.now().plusDays(5)),
            createTodo("開発環境テスト3", "UI表示確認テスト", TodoPriority.LOW, TodoStatus.COMPLETED, LocalDate.now().minusDays(1)),
            createTodo("パフォーマンステスト", "システムパフォーマンスの測定と評価", TodoPriority.HIGH, TodoStatus.PENDING, LocalDate.now().plusDays(7)),
            createTodo("セキュリティテスト", "セキュリティホール検査とペネトレーションテスト", TodoPriority.HIGH, TodoStatus.PENDING, LocalDate.now().plusDays(10))
        );
        
        todoRepository.saveAll(sampleTodos);
    }

    private Todo createRandomTodo(int index) {
        String[] titlePrefixes = {"タスク", "プロジェクト", "作業", "確認", "実装", "テスト", "調査", "レビュー"};
        String[] descriptions = {
            "重要な開発作業です。期限内に完了する必要があります。",
            "システムの機能改善のための作業項目です。",
            "品質向上のためのテスト・検証作業です。",
            "新機能の設計・実装に関する作業です。",
            "パフォーマンス最適化のための調査・改善作業です。"
        };
        
        Todo todo = new Todo();
        todo.setTitle(titlePrefixes[(int)(Math.random() * titlePrefixes.length)] + " " + index);
        todo.setDescription(descriptions[(int)(Math.random() * descriptions.length)]);
        todo.setPriority(TodoPriority.values()[(int)(Math.random() * TodoPriority.values().length)]);
        todo.setStatus(TodoStatus.values()[(int)(Math.random() * TodoStatus.values().length)]);
        todo.setDueDate(LocalDate.now().plusDays((int)(Math.random() * 30) - 5)); // -5 to +25 days
        todo.setCreatedAt(LocalDateTime.now().minusDays((int)(Math.random() * 10)));
        todo.setUpdatedAt(todo.getCreatedAt());
        
        return todo;
    }

    private Todo createTodo(String title, String description, TodoPriority priority, TodoStatus status, LocalDate dueDate) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority);
        todo.setStatus(status);
        todo.setDueDate(dueDate);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(todo.getCreatedAt());
        return todo;
    }
}