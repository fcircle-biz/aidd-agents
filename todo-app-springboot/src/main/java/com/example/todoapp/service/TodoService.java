package com.example.todoapp.service;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Todoに関するビジネスロジックを提供するサービスインターフェース
 */
public interface TodoService {
    
    /**
     * 全てのTodoをページング対応で取得
     * @param pageable ページング情報
     * @return ページング対応のTodo一覧
     */
    Page<Todo> findAll(Pageable pageable);
    
    /**
     * IDによるTodo取得
     * @param id TodoのID
     * @return 対象のTodo
     * @throws com.example.todoapp.exception.TodoNotFoundException Todoが見つからない場合
     */
    Todo findById(Long id);
    
    /**
     * 新しいTodoの作成
     * @param request Todo作成リクエスト
     * @return 作成されたTodo
     */
    Todo create(TodoRequest request);
    
    /**
     * 既存Todoの更新
     * @param id 更新対象のID
     * @param request Todo更新リクエスト
     * @return 更新されたTodo
     * @throws com.example.todoapp.exception.TodoNotFoundException Todoが見つからない場合
     */
    Todo update(Long id, TodoRequest request);
    
    /**
     * Todoの削除
     * @param id 削除対象のID
     * @throws com.example.todoapp.exception.TodoNotFoundException Todoが見つからない場合
     */
    void delete(Long id);
    
    /**
     * 検索条件によるTodo検索
     * @param criteria 検索条件
     * @return 検索結果のTodo一覧
     */
    List<Todo> search(TodoSearchCriteria criteria);
    
    /**
     * ステータスによるTodo取得
     * @param status 対象のステータス
     * @return 該当ステータスのTodo一覧
     */
    List<Todo> findByStatus(TodoStatus status);
    
    /**
     * 期限切れTodoの取得
     * @return 期限切れのTodo一覧
     */
    List<Todo> findOverdueTodos();
    
    /**
     * ステータス別の件数統計取得
     * @param status 対象のステータス
     * @return 該当ステータスの件数
     */
    long countByStatus(TodoStatus status);
}