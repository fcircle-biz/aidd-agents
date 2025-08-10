package com.example.todoapp.controller;

import com.example.todoapp.dto.TodoRequest;
import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Todo管理のWebコントローラー
 * Thymeleafテンプレートを使用したWeb UIを提供
 * 
 * @author System
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class TodoWebController {
    
    private final TodoService todoService;
    
    /**
     * ホーム画面（一覧画面へリダイレクト）
     * 
     * @return Todo一覧画面へのリダイレクト
     */
    @GetMapping("/")
    public String home() {
        log.info("Web: Redirecting to todos list");
        return "redirect:/todos";
    }
    
    /**
     * Todo一覧画面
     * ページング対応でTodoの一覧を表示
     * 
     * @param pageable ページング情報
     * @param model モデル
     * @return Todo一覧テンプレート
     */
    @GetMapping("/todos")
    public String list(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable, Model model) {
        log.info("Web: Getting todos list with pagination: {}", pageable);
        
        Page<Todo> todoPage = todoService.findAll(pageable);
        model.addAttribute("todos", todoPage);
        
        // ステータス別の統計情報を追加
        long todoCount = todoService.countByStatus(TodoStatus.TODO);
        long inProgressCount = todoService.countByStatus(TodoStatus.IN_PROGRESS);
        long doneCount = todoService.countByStatus(TodoStatus.DONE);
        
        model.addAttribute("todoCount", todoCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);
        
        log.info("Web: Displaying {} todos in page {} of {}", 
                todoPage.getNumberOfElements(), 
                todoPage.getNumber(), 
                todoPage.getTotalPages());
        
        return "todo/list";
    }
    
    /**
     * Todo作成画面
     * 新しいTodo作成フォームを表示
     * 
     * @param model モデル
     * @return Todo作成テンプレート
     */
    @GetMapping("/todos/new")
    public String showCreateForm(Model model) {
        log.info("Web: Showing todo create form");
        
        model.addAttribute("todoRequest", new TodoRequest());
        model.addAttribute("statuses", TodoStatus.values());
        model.addAttribute("priorities", TodoPriority.values());
        
        return "todo/create";
    }
    
    /**
     * Todo作成処理
     * フォームから送信されたデータで新しいTodoを作成
     * 
     * @param todoRequest Todo作成リクエスト
     * @param bindingResult バリデーション結果
     * @param model モデル
     * @param redirectAttributes リダイレクト属性
     * @return 成功時は詳細画面、失敗時は作成フォーム
     */
    @PostMapping("/todos")
    public String create(@Valid @ModelAttribute TodoRequest todoRequest,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        log.info("Web: Creating todo with title: {}", todoRequest.getTitle());
        
        if (bindingResult.hasErrors()) {
            log.warn("Web: Validation errors in todo creation: {}", bindingResult.getAllErrors());
            model.addAttribute("todoRequest", todoRequest);
            model.addAttribute("statuses", TodoStatus.values());
            model.addAttribute("priorities", TodoPriority.values());
            return "todo/create";
        }
        
        try {
            Todo createdTodo = todoService.create(todoRequest);
            log.info("Web: Successfully created todo with id: {}", createdTodo.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Todoが正常に作成されました。");
            return "redirect:/todos/" + createdTodo.getId();
        } catch (Exception e) {
            log.error("Web: Error creating todo", e);
            model.addAttribute("errorMessage", "Todoの作成中にエラーが発生しました。");
            model.addAttribute("todoRequest", todoRequest);
            model.addAttribute("statuses", TodoStatus.values());
            model.addAttribute("priorities", TodoPriority.values());
            return "todo/create";
        }
    }
    
    /**
     * Todo詳細画面
     * 指定されたIDのTodoの詳細情報を表示
     * 
     * @param id TodoのID
     * @param model モデル
     * @return Todo詳細テンプレート
     */
    @GetMapping("/todos/{id}")
    public String detail(@PathVariable Long id, Model model) {
        log.info("Web: Getting todo detail for id: {}", id);
        
        try {
            Todo todo = todoService.findById(id);
            model.addAttribute("todo", todo);
            
            log.info("Web: Found todo with id: {}", id);
            return "todo/detail";
        } catch (Exception e) {
            log.error("Web: Error getting todo detail for id: {}", id, e);
            model.addAttribute("errorMessage", "Todoが見つかりませんでした。");
            return "error/404";
        }
    }
    
    /**
     * Todo編集画面
     * 指定されたIDのTodoの編集フォームを表示
     * 
     * @param id TodoのID
     * @param model モデル
     * @return Todo編集テンプレート
     */
    @GetMapping("/todos/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("Web: Showing todo edit form for id: {}", id);
        
        try {
            Todo todo = todoService.findById(id);
            
            // TodoをTodoRequestに変換
            TodoRequest todoRequest = new TodoRequest();
            todoRequest.setTitle(todo.getTitle());
            todoRequest.setDescription(todo.getDescription());
            todoRequest.setStatus(todo.getStatus());
            todoRequest.setPriority(todo.getPriority());
            todoRequest.setDueDate(todo.getDueDate());
            
            model.addAttribute("todo", todo);
            model.addAttribute("todoRequest", todoRequest);
            model.addAttribute("statuses", TodoStatus.values());
            model.addAttribute("priorities", TodoPriority.values());
            
            log.info("Web: Loaded todo edit form for id: {}", id);
            return "todo/edit";
        } catch (Exception e) {
            log.error("Web: Error loading todo edit form for id: {}", id, e);
            model.addAttribute("errorMessage", "Todoが見つかりませんでした。");
            return "error/404";
        }
    }
    
    /**
     * Todo更新処理
     * フォームから送信されたデータで既存のTodoを更新
     * 
     * @param id 更新対象のTodoのID
     * @param todoRequest Todo更新リクエスト
     * @param bindingResult バリデーション結果
     * @param model モデル
     * @param redirectAttributes リダイレクト属性
     * @return 成功時は詳細画面、失敗時は編集フォーム
     */
    @PostMapping("/todos/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute TodoRequest todoRequest,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        log.info("Web: Updating todo with id: {}", id);
        
        if (bindingResult.hasErrors()) {
            log.warn("Web: Validation errors in todo update: {}", bindingResult.getAllErrors());
            try {
                Todo todo = todoService.findById(id);
                model.addAttribute("todo", todo);
                model.addAttribute("todoRequest", todoRequest);
                model.addAttribute("statuses", TodoStatus.values());
                model.addAttribute("priorities", TodoPriority.values());
                return "todo/edit";
            } catch (Exception e) {
                log.error("Web: Error loading todo for validation error display", e);
                return "error/404";
            }
        }
        
        try {
            Todo updatedTodo = todoService.update(id, todoRequest);
            log.info("Web: Successfully updated todo with id: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Todoが正常に更新されました。");
            return "redirect:/todos/" + updatedTodo.getId();
        } catch (Exception e) {
            log.error("Web: Error updating todo with id: {}", id, e);
            model.addAttribute("errorMessage", "Todoの更新中にエラーが発生しました。");
            try {
                Todo todo = todoService.findById(id);
                model.addAttribute("todo", todo);
                model.addAttribute("todoRequest", todoRequest);
                model.addAttribute("statuses", TodoStatus.values());
                model.addAttribute("priorities", TodoPriority.values());
                return "todo/edit";
            } catch (Exception ex) {
                log.error("Web: Error loading todo for error display", ex);
                return "error/404";
            }
        }
    }
    
    /**
     * Todo削除処理
     * 指定されたIDのTodoを削除
     * 
     * @param id 削除対象のTodoのID
     * @param redirectAttributes リダイレクト属性
     * @return Todo一覧画面へのリダイレクト
     */
    @PostMapping("/todos/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Web: Deleting todo with id: {}", id);
        
        try {
            todoService.delete(id);
            log.info("Web: Successfully deleted todo with id: {}", id);
            redirectAttributes.addFlashAttribute("successMessage", "Todoが正常に削除されました。");
        } catch (Exception e) {
            log.error("Web: Error deleting todo with id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Todoの削除中にエラーが発生しました。");
        }
        
        return "redirect:/todos";
    }
    
    /**
     * Todo検索画面
     * 検索フォームを表示し、検索条件が指定されている場合は検索結果も表示
     * 
     * @param keyword 検索キーワード
     * @param status ステータスフィルター
     * @param priority 優先度フィルター
     * @param model モデル
     * @return Todo検索テンプレート
     */
    @GetMapping("/todos/search")
    public String search(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String priority,
                        Model model) {
        log.info("Web: Todo search with keyword: {}, status: {}, priority: {}", 
                keyword, status, priority);
        
        model.addAttribute("statuses", TodoStatus.values());
        model.addAttribute("priorities", TodoPriority.values());
        
        // 検索条件をモデルに設定（検索フォームの値を保持するため）
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("selectedStatus", status != null ? status : "");
        model.addAttribute("selectedPriority", priority != null ? priority : "");
        
        // 検索条件が指定されている場合は検索を実行
        if (keyword != null || status != null || priority != null) {
            TodoSearchCriteria criteria = new TodoSearchCriteria();
            criteria.setKeyword(keyword);
            
            // ステータス文字列をEnumに変換
            if (status != null && !status.trim().isEmpty()) {
                try {
                    criteria.setStatus(TodoStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Web: Invalid status parameter: {}", status);
                }
            }
            
            // 優先度文字列をEnumに変換
            if (priority != null && !priority.trim().isEmpty()) {
                try {
                    criteria.setPriority(TodoPriority.valueOf(priority.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Web: Invalid priority parameter: {}", priority);
                }
            }
            
            List<Todo> searchResults = todoService.search(criteria);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("resultCount", searchResults.size());
            
            log.info("Web: Found {} todos matching search criteria", searchResults.size());
        }
        
        return "todo/search";
    }
}