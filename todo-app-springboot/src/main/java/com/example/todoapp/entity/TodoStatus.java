package com.example.todoapp.entity;

/**
 * Todoのステータスを表すEnumクラス
 * 
 * @author System
 */
public enum TodoStatus {
    
    /**
     * 未完了
     */
    TODO("未完了"),
    
    /**
     * 進行中
     */
    IN_PROGRESS("進行中"),
    
    /**
     * 完了
     */
    DONE("完了");
    
    private final String displayName;
    
    /**
     * コンストラクタ
     * 
     * @param displayName 表示名
     */
    TodoStatus(String displayName) {
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