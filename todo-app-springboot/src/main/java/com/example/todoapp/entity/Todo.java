package com.example.todoapp.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Todoエンティティクラス
 * 
 * @author System
 */
@Entity
@Table(name = "todo", 
    indexes = {
        @Index(name = "idx_todo_status", columnList = "status"),
        @Index(name = "idx_todo_priority", columnList = "priority"),
        @Index(name = "idx_todo_due_date", columnList = "due_date"),
        @Index(name = "idx_todo_created_at", columnList = "created_at"),
        @Index(name = "idx_todo_status_priority", columnList = "status, priority"),
        @Index(name = "idx_todo_status_due_date", columnList = "status, due_date"),
        @Index(name = "idx_todo_title_search", columnList = "title"),
        @Index(name = "idx_todo_composite_search", columnList = "status, due_date, priority")
    }
)
public class Todo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false, length = 100)
    private String title;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TodoStatus status = TodoStatus.TODO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TodoPriority priority = TodoPriority.MEDIUM;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * デフォルトコンストラクタ
     */
    public Todo() {
    }
    
    /**
     * コンストラクタ
     * 
     * @param title タイトル
     * @param description 説明
     */
    public Todo(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    /**
     * コンストラクタ
     * 
     * @param title タイトル
     * @param description 説明
     * @param status ステータス
     * @param priority 優先度
     */
    public Todo(String title, String description, TodoStatus status, TodoPriority priority) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }
    
    // Getter methods
    
    public Long getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public TodoStatus getStatus() {
        return status;
    }
    
    public TodoPriority getPriority() {
        return priority;
    }
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    // Setter methods
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setStatus(TodoStatus status) {
        this.status = status;
    }
    
    public void setPriority(TodoPriority priority) {
        this.priority = priority;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Todo todo = (Todo) o;
        return Objects.equals(id, todo.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Todo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}