package com.example.todoapp.repository;

import com.example.todoapp.entity.Todo;
import com.example.todoapp.entity.TodoStatus;
import com.example.todoapp.entity.TodoPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * TodoRepositoryインターフェース
 * Spring Data JPAを使用したTodoエンティティのデータアクセス層
 * 基本的なCRUD操作に加え、カスタムクエリメソッドとページング機能を提供
 * 
 * 要件8（JPA使用）、要件6（検索機能）、要件2（降順ソート）に対応
 * 
 * @author System
 */
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    // ===========================================
    // 基本的なCRUD操作は JpaRepository により自動実装
    // - findAll() -> List<Todo>
    // - findById(Long id) -> Optional<Todo>
    // - save(Todo todo) -> Todo
    // - deleteById(Long id) -> void
    // ===========================================
    
    /**
     * ページング対応のTodo一覧取得
     * 要件2（一覧表示の降順ソート）に対応
     * 
     * @param pageable ページング情報（ソート順、ページサイズ、ページ番号）
     * @return ページ情報を含むTodo一覧
     */
    Page<Todo> findAll(Pageable pageable);
    
    /**
     * ステータス別Todo検索
     * 要件6（検索機能）に対応
     * 
     * @param status 検索対象のステータス
     * @return 指定したステータスのTodo一覧
     */
    List<Todo> findByStatus(TodoStatus status);
    
    /**
     * タイトルまたは説明文でのキーワード検索
     * 要件6（検索機能）のキーワード検索に対応
     * 
     * @param title タイトルに含まれる検索キーワード
     * @param description 説明文に含まれる検索キーワード
     * @return キーワードに一致するTodo一覧
     */
    List<Todo> findByTitleContainingOrDescriptionContaining(String title, String description);
    
    /**
     * 期限切れTodo検索
     * 指定した日付より前の期限を持つTodoを検索
     * 要件6（検索機能）の期限による検索に対応
     * 
     * @param date 基準日（この日付より前の期限を持つTodoを検索）
     * @return 期限切れTodo一覧
     */
    List<Todo> findByDueDateBefore(LocalDate date);
    
    /**
     * ステータス別のTodo件数取得
     * ダッシュボード機能や統計表示に使用
     * 
     * @param status 集計対象のステータス
     * @return 指定したステータスのTodo件数
     */
    Long countByStatus(TodoStatus status);
    
    /**
     * 優先度別のTodo件数取得
     * ダッシュボード機能や統計表示に使用
     * 
     * @param priority 集計対象の優先度
     * @return 指定した優先度のTodo件数
     */
    Long countByPriority(TodoPriority priority);
    
    /**
     * 期限切れかつ未完了のTodo検索
     * カスタムクエリを使用して、期限切れで未完了のTodoを検索
     * 
     * @param date 基準日
     * @return 期限切れかつ未完了のTodo一覧
     */
    @Query("SELECT t FROM Todo t WHERE t.dueDate < :date AND t.status != 'DONE' ORDER BY t.dueDate ASC")
    List<Todo> findOverdueTodos(@Param("date") LocalDate date);
    
    /**
     * ステータス別ページング対応検索
     * ステータスでフィルタリングしながらページング機能を提供
     * 
     * @param status 検索対象のステータス
     * @param pageable ページング情報
     * @return ページ情報を含む指定ステータスのTodo一覧
     */
    Page<Todo> findByStatus(TodoStatus status, Pageable pageable);
    
    /**
     * キーワード検索（ページング対応）
     * タイトルまたは説明文での検索にページング機能を追加
     * 
     * @param title タイトル検索キーワード
     * @param description 説明文検索キーワード
     * @param pageable ページング情報
     * @return ページ情報を含むキーワード検索結果
     */
    Page<Todo> findByTitleContainingOrDescriptionContaining(
            String title, String description, Pageable pageable);
    
    /**
     * 複合検索（ステータス + キーワード）
     * ステータスとキーワードの両方で検索
     * 
     * @param status 検索対象のステータス
     * @param title タイトル検索キーワード
     * @param description 説明文検索キーワード
     * @param pageable ページング情報
     * @return 複合条件に一致するTodo一覧
     */
    @Query("SELECT t FROM Todo t WHERE t.status = :status " +
           "AND (t.title LIKE %:title% OR t.description LIKE %:description%) " +
           "ORDER BY t.createdAt DESC")
    Page<Todo> findByStatusAndTitleContainingOrDescriptionContaining(
            @Param("status") TodoStatus status, 
            @Param("title") String title, 
            @Param("description") String description, 
            Pageable pageable);
    
    /**
     * 作成日時の範囲検索
     * 指定した期間に作成されたTodoを検索
     * 
     * @param startDate 検索開始日時
     * @param endDate 検索終了日時
     * @return 指定期間に作成されたTodo一覧
     */
    @Query("SELECT t FROM Todo t WHERE CAST(t.createdAt AS date) >= :startDate AND CAST(t.createdAt AS date) <= :endDate ORDER BY t.createdAt DESC")
    List<Todo> findByCreatedAtBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 複合検索（ステータス + キーワード）- ページング無し版
     * ステータスとキーワードの両方で検索
     * 
     * @param title タイトル検索キーワード
     * @param description 説明文検索キーワード
     * @param status 検索対象のステータス
     * @return 複合条件に一致するTodo一覧
     */
    @Query("SELECT t FROM Todo t WHERE (t.title LIKE %:title% OR t.description LIKE %:description%) AND t.status = :status")
    List<Todo> findByTitleContainingOrDescriptionContainingAndStatus(@Param("title") String title, @Param("description") String description, @Param("status") TodoStatus status);
    
    /**
     * 期限切れかつ指定ステータス以外のTodo検索
     * 期限切れで指定ステータス以外のTodoを検索
     * 
     * @param date 基準日
     * @param status 除外するステータス
     * @return 期限切れかつ指定ステータス以外のTodo一覧
     */
    List<Todo> findByDueDateBeforeAndStatusNot(LocalDate date, TodoStatus status);
}