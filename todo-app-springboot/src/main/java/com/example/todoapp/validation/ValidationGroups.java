package com.example.todoapp.validation;

/**
 * バリデーショングループ定義
 * 異なるシナリオでの検証を行うためのマーカーインターフェース
 * 
 * @author System
 */
public class ValidationGroups {
    
    /**
     * 作成時のバリデーション
     */
    public interface Create {}
    
    /**
     * 更新時のバリデーション
     */
    public interface Update {}
    
    /**
     * 検索時のバリデーション
     */
    public interface Search {}
    
    /**
     * 基本的なバリデーション（共通）
     */
    public interface Basic {}
    
    /**
     * 厳密なバリデーション（管理者権限など）
     */
    public interface Strict {}
}