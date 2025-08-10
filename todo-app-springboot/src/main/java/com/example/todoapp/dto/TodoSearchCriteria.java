package com.example.todoapp.dto;

import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.validation.ValidDateRange;
import com.example.todoapp.validation.ValidationGroups;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Todo検索条件DTOクラス
 * 検索フィルタリングの条件を格納
 * 
 * @author System
 */
@ValidDateRange(from = "dueDateFrom", to = "dueDateTo", message = "期限開始日は期限終了日より前である必要があります", groups = ValidationGroups.Search.class)
@ValidDateRange(from = "createdFrom", to = "createdTo", message = "作成日開始日は作成日終了日より前である必要があります", groups = ValidationGroups.Search.class)
public class TodoSearchCriteria {
    
    /**
     * キーワード検索（タイトル・説明に対する部分一致検索）
     */
    @Size(max = 100, message = "検索キーワードは100文字以内で入力してください", groups = ValidationGroups.Search.class)
    private String keyword;
    
    /**
     * ステータスフィルター
     */
    private TodoStatus status;
    
    /**
     * 優先度フィルター
     */
    private TodoPriority priority;
    
    /**
     * 期限開始日（この日以降の期限を検索）
     */
    private LocalDate dueDateFrom;
    
    /**
     * 期限終了日（この日以前の期限を検索）
     */
    private LocalDate dueDateTo;
    
    /**
     * 作成日開始日（この日以降に作成されたTodoを検索）
     */
    private LocalDate createdFrom;
    
    /**
     * 作成日終了日（この日以前に作成されたTodoを検索）
     */
    private LocalDate createdTo;
    
    /**
     * デフォルトコンストラクタ
     */
    public TodoSearchCriteria() {
    }
    
    /**
     * コンストラクタ
     * 
     * @param keyword キーワード
     * @param status ステータス
     * @param priority 優先度
     */
    public TodoSearchCriteria(String keyword, TodoStatus status, TodoPriority priority) {
        this.keyword = keyword;
        this.status = status;
        this.priority = priority;
    }
    
    /**
     * 検索条件が空かどうかを判定する
     * 
     * @return すべての検索条件が空の場合はtrue
     */
    public boolean isEmpty() {
        return (keyword == null || keyword.trim().isEmpty()) &&
               status == null &&
               priority == null &&
               dueDateFrom == null &&
               dueDateTo == null &&
               createdFrom == null &&
               createdTo == null;
    }
    
    /**
     * キーワード検索の条件があるかどうかを判定する
     * 
     * @return キーワードが設定されている場合はtrue
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 期限での絞り込み条件があるかどうかを判定する
     * 
     * @return 期限の検索条件が設定されている場合はtrue
     */
    public boolean hasDueDateRange() {
        return dueDateFrom != null || dueDateTo != null;
    }
    
    /**
     * 作成日での絞り込み条件があるかどうかを判定する
     * 
     * @return 作成日の検索条件が設定されている場合はtrue
     */
    public boolean hasCreatedDateRange() {
        return createdFrom != null || createdTo != null;
    }
    
    // Getter methods
    
    public String getKeyword() {
        return keyword;
    }
    
    public TodoStatus getStatus() {
        return status;
    }
    
    public TodoPriority getPriority() {
        return priority;
    }
    
    public LocalDate getDueDateFrom() {
        return dueDateFrom;
    }
    
    public LocalDate getDueDateTo() {
        return dueDateTo;
    }
    
    public LocalDate getCreatedFrom() {
        return createdFrom;
    }
    
    public LocalDate getCreatedTo() {
        return createdTo;
    }
    
    // Setter methods
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public void setStatus(TodoStatus status) {
        this.status = status;
    }
    
    public void setPriority(TodoPriority priority) {
        this.priority = priority;
    }
    
    public void setDueDateFrom(LocalDate dueDateFrom) {
        this.dueDateFrom = dueDateFrom;
    }
    
    public void setDueDateTo(LocalDate dueDateTo) {
        this.dueDateTo = dueDateTo;
    }
    
    public void setCreatedFrom(LocalDate createdFrom) {
        this.createdFrom = createdFrom;
    }
    
    public void setCreatedTo(LocalDate createdTo) {
        this.createdTo = createdTo;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoSearchCriteria that = (TodoSearchCriteria) o;
        return Objects.equals(keyword, that.keyword) &&
               status == that.status &&
               priority == that.priority &&
               Objects.equals(dueDateFrom, that.dueDateFrom) &&
               Objects.equals(dueDateTo, that.dueDateTo) &&
               Objects.equals(createdFrom, that.createdFrom) &&
               Objects.equals(createdTo, that.createdTo);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(keyword, status, priority, dueDateFrom, dueDateTo, createdFrom, createdTo);
    }
    
    @Override
    public String toString() {
        return "TodoSearchCriteria{" +
                "keyword='" + keyword + '\'' +
                ", status=" + status +
                ", priority=" + priority +
                ", dueDateFrom=" + dueDateFrom +
                ", dueDateTo=" + dueDateTo +
                ", createdFrom=" + createdFrom +
                ", createdTo=" + createdTo +
                '}';
    }
}