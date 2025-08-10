package com.example.todoapp.service.impl;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.exception.TodoNotFoundException;
import com.example.todoapp.repository.TodoRepository;
import com.example.todoapp.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Todoに関するビジネスロジックの実装クラス
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TodoServiceImpl implements TodoService {
    
    private final TodoRepository todoRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<Todo> findAll(Pageable pageable) {
        log.info("Finding all todos with pagination: {}", pageable);
        return todoRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        log.info("Finding todo by id: {}", id);
        return todoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Todo not found with id: {}", id);
                    return new TodoNotFoundException(id);
                });
    }
    
    @Override
    public Todo create(TodoRequest request) {
        log.info("Creating new todo with title: {}", request.getTitle());
        
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        todo.setDescription(request.getDescription());
        todo.setStatus(request.getStatus() != null ? request.getStatus() : TodoStatus.TODO);
        todo.setPriority(request.getPriority() != null ? request.getPriority() : TodoPriority.MEDIUM);
        todo.setDueDate(request.getDueDate());
        
        // 作成日時と更新日時を自動設定
        LocalDateTime now = LocalDateTime.now();
        todo.setCreatedAt(now);
        todo.setUpdatedAt(now);
        
        Todo savedTodo = todoRepository.save(todo);
        log.info("Created todo with id: {}", savedTodo.getId());
        
        return savedTodo;
    }
    
    @Override
    public Todo update(Long id, TodoRequest request) {
        log.info("Updating todo with id: {}", id);
        
        Todo existingTodo = findById(id); // 存在チェックを兼ねる
        
        // 更新内容を設定
        existingTodo.setTitle(request.getTitle());
        existingTodo.setDescription(request.getDescription());
        existingTodo.setStatus(request.getStatus() != null ? request.getStatus() : existingTodo.getStatus());
        existingTodo.setPriority(request.getPriority() != null ? request.getPriority() : existingTodo.getPriority());
        existingTodo.setDueDate(request.getDueDate());
        
        // 更新日時を自動更新
        existingTodo.setUpdatedAt(LocalDateTime.now());
        
        Todo updatedTodo = todoRepository.save(existingTodo);
        log.info("Updated todo with id: {}", updatedTodo.getId());
        
        return updatedTodo;
    }
    
    @Override
    public void delete(Long id) {
        log.info("Deleting todo with id: {}", id);
        
        // 存在チェック
        if (!todoRepository.existsById(id)) {
            log.warn("Attempted to delete non-existent todo with id: {}", id);
            throw new TodoNotFoundException(id);
        }
        
        todoRepository.deleteById(id);
        log.info("Deleted todo with id: {}", id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Todo> search(TodoSearchCriteria criteria) {
        log.info("Searching todos with criteria: {}", criteria);
        
        if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
            String keyword = criteria.getKeyword().trim();
            if (criteria.getStatus() != null) {
                // キーワードとステータスの両方で検索
                return todoRepository.findByTitleContainingOrDescriptionContainingAndStatus(
                    keyword, keyword, criteria.getStatus());
            } else {
                // キーワードのみで検索
                return todoRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
            }
        } else if (criteria.getStatus() != null) {
            // ステータスのみで検索
            return todoRepository.findByStatus(criteria.getStatus());
        } else {
            // 条件が指定されていない場合は全件取得
            return todoRepository.findAll();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Todo> findByStatus(TodoStatus status) {
        log.info("Finding todos by status: {}", status);
        return todoRepository.findByStatus(status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Todo> findOverdueTodos() {
        log.info("Finding overdue todos");
        LocalDate today = LocalDate.now();
        return todoRepository.findByDueDateBeforeAndStatusNot(today, TodoStatus.DONE);
    }
}