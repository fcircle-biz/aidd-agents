package com.example.todoapp.validation;

import com.example.todoapp.dto.TodoSearchCriteria;
import com.example.todoapp.entity.TodoPriority;
import com.example.todoapp.entity.TodoStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TodoSearchCriteriaバリデーションテスト
 * 
 * @author System
 */
class TodoSearchCriteriaValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidSearchCriteria() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("test");
        criteria.setStatus(TodoStatus.TODO);
        criteria.setPriority(TodoPriority.HIGH);
        criteria.setDueDateFrom(LocalDate.of(2024, 1, 1));
        criteria.setDueDateTo(LocalDate.of(2024, 12, 31));
        criteria.setCreatedFrom(LocalDate.of(2024, 1, 1));
        criteria.setCreatedTo(LocalDate.of(2024, 12, 31));
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testKeywordTooLong() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("a".repeat(101)); // 101文字
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("keyword")));
    }
    
    @Test
    void testInvalidDueDateRange() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setDueDateFrom(LocalDate.of(2024, 12, 31));
        criteria.setDueDateTo(LocalDate.of(2024, 1, 1)); // from > to
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("開始日") && v.getMessage().contains("終了日")));
    }
    
    @Test
    void testInvalidCreatedDateRange() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setCreatedFrom(LocalDate.of(2024, 12, 31));
        criteria.setCreatedTo(LocalDate.of(2024, 1, 1)); // from > to
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("開始日") && v.getMessage().contains("終了日")));
    }
    
    @Test
    void testValidDueDateRangeWithSameDate() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        LocalDate sameDate = LocalDate.of(2024, 6, 15);
        criteria.setDueDateFrom(sameDate);
        criteria.setDueDateTo(sameDate); // 同じ日付はOK
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testPartialDueDateRange() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setDueDateFrom(LocalDate.of(2024, 1, 1));
        // dueDateTo は null
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertTrue(violations.isEmpty()); // 片方だけでもOK
    }
    
    @Test
    void testEmptySearchCriteria() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        assertTrue(violations.isEmpty()); // 空の検索条件もOK
        assertTrue(criteria.isEmpty()); // isEmpty() メソッドのテストも兼ねる
    }
    
    @Test
    void testMultipleValidationErrors() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setKeyword("a".repeat(101)); // キーワード長すぎ
        criteria.setDueDateFrom(LocalDate.of(2024, 12, 31));
        criteria.setDueDateTo(LocalDate.of(2024, 1, 1)); // 日付範囲無効
        criteria.setCreatedFrom(LocalDate.of(2024, 12, 31));
        criteria.setCreatedTo(LocalDate.of(2024, 1, 1)); // 日付範囲無効
        
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        // 複数のエラーがあることを確認
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3); // キーワード + 期限範囲 + 作成日範囲
    }
    
    @Test
    void testHelperMethods() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        
        // 初期状態
        assertTrue(criteria.isEmpty());
        assertFalse(criteria.hasKeyword());
        assertFalse(criteria.hasDueDateRange());
        assertFalse(criteria.hasCreatedDateRange());
        
        // キーワード設定
        criteria.setKeyword("test");
        assertFalse(criteria.isEmpty());
        assertTrue(criteria.hasKeyword());
        
        // 空のキーワード
        criteria.setKeyword("");
        assertFalse(criteria.hasKeyword());
        
        // 期限範囲設定
        criteria.setDueDateFrom(LocalDate.now());
        assertTrue(criteria.hasDueDateRange());
        
        // 作成日範囲設定
        criteria.setCreatedTo(LocalDate.now());
        assertTrue(criteria.hasCreatedDateRange());
    }
}