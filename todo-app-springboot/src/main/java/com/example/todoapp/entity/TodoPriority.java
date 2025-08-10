package com.example.todoapp.entity;

/**
 * Todoの優先度を表すEnumクラス
 * 
 * @author System
 */
public enum TodoPriority {
    
    /**
     * 低
     */
    LOW("低"),
    
    /**
     * 中
     */
    MEDIUM("中"),
    
    /**
     * 高
     */
    HIGH("高");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    TodoPriority(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * 表示名を取得する
     * 
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }
}