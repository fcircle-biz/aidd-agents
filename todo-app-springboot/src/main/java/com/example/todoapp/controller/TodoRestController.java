package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoResponse;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.service.TodoService;
import com.example.todoapp.util.TodoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Todo管理のRESTful APIコントローラー
 * JSON形式でのCRUD操作と検索機能を提供
 * 
 * @author System
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Slf4j
public class TodoRestController {
    
    private final TodoService todoService;
    
    /**
     * Todo一覧取得エンドポイント
     * ページング対応でTodoの一覧をJSON形式で返却
     * 
     * @param pageable ページング情報（page, size, sort）
     * @return ページング対応のTodo一覧レスポンス
     */
    @GetMapping
    public ResponseEntity<Page<TodoResponse>> getAllTodos(Pageable pageable) {
        log.info("REST API: Getting all todos with pagination: {}", pageable);
        
        Page<Todo> todoPage = todoService.findAll(pageable);
        List<TodoResponse> responseList = TodoMapper.toResponseList(todoPage.getContent());
        Page<TodoResponse> responsePage = new PageImpl<>(
            responseList, 
            pageable, 
            todoPage.getTotalElements()
        );
        
        log.info("REST API: Returning {} todos in page {} of {}", 
                responsePage.getNumberOfElements(), 
                responsePage.getNumber(), 
                responsePage.getTotalPages());
        
        return ResponseEntity.ok(responsePage);
    }
    
    /**
     * Todo詳細取得エンドポイント
     * 指定されたIDのTodoの詳細情報をJSON形式で返却
     * 
     * @param id TodoのID
     * @return Todo詳細レスポンス、存在しない場合は404
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(@PathVariable Long id) {
        log.info("REST API: Getting todo by id: {}", id);
        
        Todo todo = todoService.findById(id);
        TodoResponse response = TodoMapper.toResponse(todo);
        
        log.info("REST API: Found todo with id: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Todo作成エンドポイント
     * 新しいTodoを作成し、作成されたTodoの情報をJSON形式で返却
     * 
     * @param request Todo作成リクエスト
     * @return 作成されたTodo情報、201 Createdステータス
     */
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody TodoRequest request) {
        log.info("REST API: Creating new todo with title: {}", request.getTitle());
        
        Todo createdTodo = todoService.create(request);
        TodoResponse response = TodoMapper.toResponse(createdTodo);
        
        // Locationヘッダーに作成されたリソースのURIを設定
        URI location = URI.create("/api/todos/" + createdTodo.getId());
        
        log.info("REST API: Created todo with id: {}", createdTodo.getId());
        return ResponseEntity.created(location).body(response);
    }
    
    /**
     * Todo更新エンドポイント
     * 既存のTodoを更新し、更新されたTodoの情報をJSON形式で返却
     * 
     * @param id 更新対象のTodoのID
     * @param request Todo更新リクエスト
     * @return 更新されたTodo情報、200 OKステータス
     */
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable Long id, 
            @Valid @RequestBody TodoRequest request) {
        log.info("REST API: Updating todo with id: {}", id);
        
        Todo updatedTodo = todoService.update(id, request);
        TodoResponse response = TodoMapper.toResponse(updatedTodo);
        
        log.info("REST API: Updated todo with id: {}", id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Todo削除エンドポイント
     * 指定されたIDのTodoを削除
     * 
     * @param id 削除対象のTodoのID
     * @return 204 No Contentステータス
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(@PathVariable Long id) {
        log.info("REST API: Deleting todo with id: {}", id);
        
        todoService.delete(id);
        
        log.info("REST API: Deleted todo with id: {}", id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Todo検索エンドポイント
     * 検索条件に基づいてTodoを検索し、結果をJSON形式で返却
     * 
     * @param keyword 検索キーワード（タイトルまたは説明に含まれる文字列）
     * @param status ステータスフィルター
     * @param priority 優先度フィルター
     * @return 検索結果のTodo一覧
     */
    @GetMapping("/search")
    public ResponseEntity<List<TodoResponse>> searchTodos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        log.info("REST API: Searching todos with keyword: {}, status: {}, priority: {}", 
                keyword, status, priority);
        
        // 検索条件DTOを作成
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword(keyword);
        
        // ステータス文字列をEnumに変換
        if (status != null && !status.trim().isEmpty()) {
            try {
                criteria.setStatus(com.example.todoapp.entity.TodoStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status parameter: {}", status);
                // 無効なステータスの場合は無視して検索を続行
            }
        }
        
        // 優先度文字列をEnumに変換
        if (priority != null && !priority.trim().isEmpty()) {
            try {
                criteria.setPriority(com.example.todoapp.entity.TodoPriority.valueOf(priority.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid priority parameter: {}", priority);
                // 無効な優先度の場合は無視して検索を続行
            }
        }
        
        List<Todo> searchResults = todoService.search(criteria);
        List<TodoResponse> responseList = TodoMapper.toResponseList(searchResults);
        
        log.info("REST API: Found {} todos matching search criteria", responseList.size());
        return ResponseEntity.ok(responseList);
    }
}