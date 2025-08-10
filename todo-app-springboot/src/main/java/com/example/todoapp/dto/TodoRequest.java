package com.example.todoapp.dto;

import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Todo作成・更新時のリクエストDTOクラス
 * バリデーションアノテーションを含む
 * 
 * @author System
 */
public class TodoRequest {
    
    /**
     * タイトル（必須、100文字以内）
     */
    @NotBlank(message = "タイトルは必須項目です")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください")
    private String title;
    
    /**
     * 説明（任意、500文字以内）
     */
    @Size(max = 500, message = "説明は500文字以内で入力してください")
    private String description;
    
    /**
     * ステータス（必須）
     */
    @NotNull(message = "ステータスは必須項目です")
    private TodoStatus status;
    
    /**
     * 優先度（必須）
     */
    @NotNull(message = "優先度は必須項目です")
    private TodoPriority priority;
    
    /**
     * 期限（任意、未来の日付のみ許可）
     */
    @Future(message = "期限は未来の日付を指定してください")
    private LocalDate dueDate;
    
    /**
     * デフォルトコンストラクタ
     */
    public TodoRequest() {
        // デフォルト値を設定
        this.status = TodoStatus.TODO;
        this.priority = TodoPriority.MEDIUM;
    }
    
    /**
     * コンストラクタ
     * 
     * @param title タイトル
     * @param description 説明
     * @param status ステータス
     * @param priority 優先度
     * @param dueDate 期限
     */
    public TodoRequest(String title, String description, TodoStatus status, TodoPriority priority, LocalDate dueDate) {
        this.title = title;
        this.description = description;
        this.status = status != null ? status : TodoStatus.TODO;
        this.priority = priority != null ? priority : TodoPriority.MEDIUM;
        this.dueDate = dueDate;
    }
    
    // Getter methods
    
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
    
    // Setter methods
    
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoRequest that = (TodoRequest) o;
        return Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               status == that.status &&
               priority == that.priority &&
               Objects.equals(dueDate, that.dueDate);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, description, status, priority, dueDate);
    }
    
    @Override
    public String toString() {
        return "TodoRequest{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                '}';
    }
}