package com.example.todoapp.util;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoResponse;
import com.example.todoapp.entity.Todo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TodoエンティティとDTOの変換を行うユーティリティクラス
 * 
 * @author System
 */
public class TodoMapper {
    
    /**
     * プライベートコンストラクタ（ユーティリティクラスのためインスタンス化を防ぐ）
     */
    private TodoMapper() {
        throw new IllegalStateException("Utility class");
    }
    
    /**
     * TodoエンティティをTodoResponseに変換する
     * 
     * @param todo Todoエンティティ
     * @return TodoResponse、引数がnullの場合はnull
     */
    public static TodoResponse toResponse(Todo todo) {
        if (todo == null) {
            return null;
        }
        
        TodoResponse response = new TodoResponse();
        response.setId(todo.getId());
        response.setTitle(todo.getTitle());
        response.setDescription(todo.getDescription());
        response.setStatus(todo.getStatus());
        response.setPriority(todo.getPriority());
        response.setDueDate(todo.getDueDate());
        response.setCreatedAt(todo.getCreatedAt());
        response.setUpdatedAt(todo.getUpdatedAt());
        
        return response;
    }
    
    /**
     * TodoエンティティのリストをTodoResponseのリストに変換する
     * 
     * @param todos Todoエンティティのリスト
     * @return TodoResponseのリスト、引数がnullの場合は空のリスト
     */
    public static List<TodoResponse> toResponseList(List<Todo> todos) {
        if (todos == null) {
            return List.of();
        }
        
        return todos.stream()
                   .map(TodoMapper::toResponse)
                   .collect(Collectors.toList());
    }
    
    /**
     * TodoRequestをTodoエンティティに変換する（新規作成用）
     * 
     * @param request TodoRequest
     * @return Todoエンティティ、引数がnullの場合はnull
     */
    public static Todo toEntity(TodoRequest request) {
        if (request == null) {
            return null;
        }
        
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setStatus(request.getStatus());
        todo.setPriority(request.getPriority());
        todo.setDueDate(request.getDueDate());
        // createdAt, updatedAtは自動設定されるため設定しない
        
        return todo;
    }
    
    /**
     * TodoRequestの内容で既存のTodoエンティティを更新する（更新用）
     * IDや作成日時などは変更しない
     * 
     * @param existingTodo 更新対象の既存Todoエンティティ
     * @param request 更新内容を含むTodoRequest
     * @return 更新されたTodoエンティティ、引数がnullの場合はnull
     */
    public static Todo updateEntity(Todo existingTodo, TodoRequest request) {
        if (existingTodo == null || request == null) {
            return null;
        }
        
        existingTodo.setTitle(request.getTitle());
        existingTodo.setDescription(request.getDescription());
        existingTodo.setStatus(request.getStatus());
        existingTodo.setPriority(request.getPriority());
        existingTodo.setDueDate(request.getDueDate());
        // IDやcreatedAt、updatedAt（自動更新）は変更しない
        
        return existingTodo;
    }
    
    /**
     * TodoエンティティからTodoRequestを生成する（編集フォーム初期化用）
     * 
     * @param todo Todoエンティティ
     * @return TodoRequest、引数がnullの場合はnull
     */
    public static TodoRequest toRequest(Todo todo) {
        if (todo == null) {
            return null;
        }
        
        TodoRequest request = new TodoRequest();
        request.setTitle(todo.getTitle());
        request.setDescription(todo.getDescription());
        request.setStatus(todo.getStatus());
        request.setPriority(todo.getPriority());
        request.setDueDate(todo.getDueDate());
        
        return request;
    }
    
    /**
     * TodoRequestのバリデーション結果に基づいてレスポンス用のエラー情報を作成する
     * （将来的にバリデーションエラーのレスポンス作成で使用する可能性があるため定義）
     * 
     * @param request TodoRequest
     * @return 基本的なTodoResponse（IDやタイムスタンプは未設定）
     */
    public static TodoResponse createResponseFromRequest(TodoRequest request) {
        if (request == null) {
            return null;
        }
        
        TodoResponse response = new TodoResponse();
        response.setTitle(request.getTitle());
        response.setDescription(request.getDescription());
        response.setStatus(request.getStatus());
        response.setPriority(request.getPriority());
        response.setDueDate(request.getDueDate());
        // ID、createdAt、updatedAtは未設定
        
        return response;
    }
}