package com.example.todoapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * 日付範囲バリデーター
 * ValidDateRangeアノテーションの実装
 * 
 * @author System
 */
public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
    
    private String fromFieldName;
    private String toFieldName;
    private boolean required;
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        this.fromFieldName = constraintAnnotation.from();
        this.toFieldName = constraintAnnotation.to();
        this.required = constraintAnnotation.required();
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        try {
            LocalDate fromDate = getFieldValue(value, fromFieldName);
            LocalDate toDate = getFieldValue(value, toFieldName);
            
            // 必須チェック
            if (required) {
                if (fromDate == null || toDate == null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("開始日と終了日の両方が必須です")
                           .addConstraintViolation();
                    return false;
                }
            }
            
            // 両方がnullの場合はOK
            if (fromDate == null && toDate == null) {
                return true;
            }
            
            // 片方だけnullの場合はOK（必須でない場合）
            if (fromDate == null || toDate == null) {
                return true;
            }
            
            // 日付範囲チェック
            if (fromDate.isAfter(toDate)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("開始日(" + fromDate + ")は終了日(" + toDate + ")より前である必要があります")
                       .addConstraintViolation();
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            // フィールドアクセスエラー
            return false;
        }
    }
    
    /**
     * リフレクションを使用してフィールドの値を取得
     */
    private LocalDate getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (LocalDate) field.get(object);
    }
}