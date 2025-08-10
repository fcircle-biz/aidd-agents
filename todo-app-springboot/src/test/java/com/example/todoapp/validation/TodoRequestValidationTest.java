package com.example.todoapp.validation;

import com.example.todoapp.dto.TodoRequest;
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
 * TodoRequestバリデーションテスト
 * 
 * @author System
 */
class TodoRequestValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidTodoRequest() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Valid Todo");
        request.setDescription("Valid description");
        request.setStatus(TodoStatus.TODO);
        request.setPriority(TodoPriority.MEDIUM);
        request.setDueDate(LocalDate.now().plusDays(1));
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testBlankTitle() {
        TodoRequest request = new TodoRequest();
        request.setTitle("");
        request.setDescription("Valid description");
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testNullTitle() {
        TodoRequest request = new TodoRequest();
        request.setTitle(null);
        request.setDescription("Valid description");
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testTitleTooLong() {
        TodoRequest request = new TodoRequest();
        request.setTitle("a".repeat(101)); // 101文字
        request.setDescription("Valid description");
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }
    
    @Test
    void testDescriptionTooLong() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Valid Title");
        request.setDescription("a".repeat(501)); // 501文字
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }
    
    @Test
    void testPastDueDate() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid description");
        request.setDueDate(LocalDate.now().minusDays(1)); // 昨日の日付
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("dueDate")));
    }
    
    @Test
    void testTodayDueDate() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid description");
        request.setDueDate(LocalDate.now()); // 今日の日付
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertTrue(violations.isEmpty()); // 今日の日付はOK
    }
    
    @Test
    void testFutureDueDate() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid description");
        request.setDueDate(LocalDate.now().plusDays(7)); // 1週間後
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertTrue(violations.isEmpty()); // 未来の日付はOK
    }
    
    @Test
    void testNullDueDate() {
        TodoRequest request = new TodoRequest();
        request.setTitle("Valid Title");
        request.setDescription("Valid description");
        request.setDueDate(null); // null
        
        Set<ConstraintViolation<TodoRequest>> violations = 
            validator.validate(request, ValidationGroups.Create.class);
        
        assertTrue(violations.isEmpty()); // nullはOK（任意項目）
    }
    
    @Test
    void testValidationGroups() {
        TodoRequest request = new TodoRequest();
        request.setTitle(""); // 無効なタイトル
        
        // Create群でのバリデーション
        Set<ConstraintViolation<TodoRequest>> createViolations = 
            validator.validate(request, ValidationGroups.Create.class);
        assertFalse(createViolations.isEmpty());
        
        // Update群でのバリデーション
        Set<ConstraintViolation<TodoRequest>> updateViolations = 
            validator.validate(request, ValidationGroups.Update.class);
        assertFalse(updateViolations.isEmpty());
        
        // 群を指定しないバリデーション（Default群のみ）
        Set<ConstraintViolation<TodoRequest>> defaultViolations = 
            validator.validate(request);
        assertTrue(defaultViolations.isEmpty()); // Default群にはバリデーションなし
    }
}