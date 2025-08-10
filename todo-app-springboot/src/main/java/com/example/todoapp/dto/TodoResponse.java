package com.example.todoapp.dto;

import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TodoのレスポンスDTOクラス
 * JSON形式でのレスポンス用データを格納
 * 
 * @author System
 */
public class TodoResponse {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * タイトル
     */
    private String title;
    
    /**
     * 説明
     */
    private String description;
    
    /**
     * ステータス
     */
    private TodoStatus status;
    
    /**
     * ステータス表示名
     */
    private String statusDisplayName;
    
    /**
     * 優先度
     */
    private TodoPriority priority;
    
    /**
     * 優先度表示名
     */
    private String priorityDisplayName;
    
    /**
     * 期限
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    
    /**
     * 作成日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新日時
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * デフォルトコンストラクタ
     */
    public TodoResponse() {
    }
    
    /**
     * コンストラクタ
     * 
     * @param id ID
     * @param title タイトル
     * @param description 説明
     * @param status ステータス
     * @param priority 優先度
     * @param dueDate 期限
     * @param createdAt 作成日時
     * @param updatedAt 更新日時
     */
    public TodoResponse(Long id, String title, String description, TodoStatus status, 
                       TodoPriority priority, LocalDate dueDate, 
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.statusDisplayName = status != null ? status.getDisplayName() : null;
        this.priority = priority;
        this.priorityDisplayName = priority != null ? priority.getDisplayName() : null;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
    
    public String getStatusDisplayName() {
        return statusDisplayName;
    }
    
    public TodoPriority getPriority() {
        return priority;
    }
    
    public String getPriorityDisplayName() {
        return priorityDisplayName;
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
        this.statusDisplayName = status != null ? status.getDisplayName() : null;
    }
    
    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }
    
    public void setPriority(TodoPriority priority) {
        this.priority = priority;
        this.priorityDisplayName = priority != null ? priority.getDisplayName() : null;
    }
    
    public void setPriorityDisplayName(String priorityDisplayName) {
        this.priorityDisplayName = priorityDisplayName;
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
        TodoResponse that = (TodoResponse) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TodoResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", statusDisplayName='" + statusDisplayName + '\'' +
                ", priority=" + priority +
                ", priorityDisplayName='" + priorityDisplayName + '\'' +
                ", dueDate=" + dueDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}