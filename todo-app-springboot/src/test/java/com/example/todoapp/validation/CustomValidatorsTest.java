package com.example.todoapp.validation;

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
 * カスタムバリデーターテスト
 * 
 * @author System
 */
class CustomValidatorsTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testNotPastDateValidator() {
        TestNotPastDate testObj = new TestNotPastDate();
        
        // 今日の日付 - OK
        testObj.date = LocalDate.now();
        Set<ConstraintViolation<TestNotPastDate>> violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 未来の日付 - OK
        testObj.date = LocalDate.now().plusDays(1);
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 過去の日付 - エラー
        testObj.date = LocalDate.now().minusDays(1);
        violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
        
        // null - OK
        testObj.date = null;
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testNotPastDateValidatorExcludeToday() {
        TestNotPastDateExcludeToday testObj = new TestNotPastDateExcludeToday();
        
        // 今日の日付 - エラー（includeToday = false）
        testObj.date = LocalDate.now();
        Set<ConstraintViolation<TestNotPastDateExcludeToday>> violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
        
        // 未来の日付 - OK
        testObj.date = LocalDate.now().plusDays(1);
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 過去の日付 - エラー
        testObj.date = LocalDate.now().minusDays(1);
        violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
    }
    
    @Test
    void testValidDateRangeValidator() {
        TestValidDateRange testObj = new TestValidDateRange();
        
        // 正常な範囲 - OK
        testObj.fromDate = LocalDate.of(2024, 1, 1);
        testObj.toDate = LocalDate.of(2024, 12, 31);
        Set<ConstraintViolation<TestValidDateRange>> violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 同じ日付 - OK
        testObj.fromDate = LocalDate.of(2024, 6, 15);
        testObj.toDate = LocalDate.of(2024, 6, 15);
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 逆順 - エラー
        testObj.fromDate = LocalDate.of(2024, 12, 31);
        testObj.toDate = LocalDate.of(2024, 1, 1);
        violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
        
        // 片方がnull - OK
        testObj.fromDate = LocalDate.of(2024, 1, 1);
        testObj.toDate = null;
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        testObj.fromDate = null;
        testObj.toDate = LocalDate.of(2024, 12, 31);
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 両方がnull - OK
        testObj.fromDate = null;
        testObj.toDate = null;
        violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
    }
    
    @Test
    void testValidDateRangeValidatorRequired() {
        TestValidDateRangeRequired testObj = new TestValidDateRangeRequired();
        
        // 両方設定 - OK
        testObj.fromDate = LocalDate.of(2024, 1, 1);
        testObj.toDate = LocalDate.of(2024, 12, 31);
        Set<ConstraintViolation<TestValidDateRangeRequired>> violations = validator.validate(testObj);
        assertTrue(violations.isEmpty());
        
        // 片方がnull - エラー（required = true）
        testObj.fromDate = LocalDate.of(2024, 1, 1);
        testObj.toDate = null;
        violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
        
        testObj.fromDate = null;
        testObj.toDate = LocalDate.of(2024, 12, 31);
        violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
        
        // 両方がnull - エラー（required = true）
        testObj.fromDate = null;
        testObj.toDate = null;
        violations = validator.validate(testObj);
        assertFalse(violations.isEmpty());
    }
    
    // テスト用クラス
    static class TestNotPastDate {
        @NotPastDate
        LocalDate date;
    }
    
    static class TestNotPastDateExcludeToday {
        @NotPastDate(includeToday = false)
        LocalDate date;
    }
    
    @ValidDateRange(from = "fromDate", to = "toDate")
    static class TestValidDateRange {
        LocalDate fromDate;
        LocalDate toDate;
    }
    
    @ValidDateRange(from = "fromDate", to = "toDate", required = true)
    static class TestValidDateRangeRequired {
        LocalDate fromDate;
        LocalDate toDate;
    }
}