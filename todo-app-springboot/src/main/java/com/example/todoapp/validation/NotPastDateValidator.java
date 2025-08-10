package com.example.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * 過去日付バリデーター
 * NotPastDateアノテーションの実装
 * 
 * @author System
 */
public class NotPastDateValidator implements ConstraintValidator<NotPastDate, LocalDate> {
    
    private boolean includeToday;
    
    @Override
    public void initialize(NotPastDate constraintAnnotation) {
        this.includeToday = constraintAnnotation.includeToday();
    }
    
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // nullの場合はOK（@NotNullで別途チェック）
        }
        
        LocalDate today = LocalDate.now();
        
        if (includeToday) {
            // 今日を含む（今日以降はOK）
            return !date.isBefore(today);
        } else {
            // 今日は含まない（明日以降のみOK）
            return date.isAfter(today);
        }
    }
}