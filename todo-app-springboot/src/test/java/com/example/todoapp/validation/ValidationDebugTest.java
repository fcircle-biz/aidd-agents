package com.example.todoapp.validation;

import com.example.todoapp.dto.TodoSearchCriteria;
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
 * バリデーションのデバッグ用テスト
 * 
 * @author System
 */
class ValidationDebugTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void debugDateRangeValidation() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setDueDateFrom(LocalDate.of(2024, 12, 31));
        criteria.setDueDateTo(LocalDate.of(2024, 1, 1)); // from > to
        
        // ValidationGroups.Search.classを指定して検証
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria, ValidationGroups.Search.class);
        
        System.out.println("Number of violations: " + violations.size());
        for (ConstraintViolation<TodoSearchCriteria> violation : violations) {
            System.out.println("Violation: " + violation.getMessage());
            System.out.println("Property path: " + violation.getPropertyPath());
            System.out.println("Invalid value: " + violation.getInvalidValue());
            System.out.println("Root bean class: " + violation.getRootBeanClass());
            System.out.println("Constraint descriptor: " + violation.getConstraintDescriptor().getAnnotation());
            System.out.println("---");
        }
        
        // 検証エラーがあることをアサート
        assertFalse(violations.isEmpty(), "Validation should find errors");
    }
    
    @Test
    void debugWithoutValidationGroups() {
        TodoSearchCriteria criteria = new TodoSearchCriteria();
        criteria.setDueDateFrom(LocalDate.of(2024, 12, 31));
        criteria.setDueDateTo(LocalDate.of(2024, 1, 1)); // from > to
        
        // ValidationGroupsを指定せずに検証
        Set<ConstraintViolation<TodoSearchCriteria>> violations = 
            validator.validate(criteria);
        
        System.out.println("Without validation groups - Number of violations: " + violations.size());
        for (ConstraintViolation<TodoSearchCriteria> violation : violations) {
            System.out.println("Violation: " + violation.getMessage());
            System.out.println("Property path: " + violation.getPropertyPath());
            System.out.println("---");
        }
    }
}