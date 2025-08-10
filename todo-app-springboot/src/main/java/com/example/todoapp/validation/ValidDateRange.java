package com.example.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * カスタムバリデーションアノテーション - 日付範囲の妥当性チェック
 * from日付がto日付より前であることを確認する
 * 
 * @author System
 */
@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidDateRanges.class)
public @interface ValidDateRange {
    
    String message() default "開始日は終了日より前である必要があります";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 開始日フィールド名
     */
    String from();
    
    /**
     * 終了日フィールド名  
     */
    String to();
    
    /**
     * 必須チェックを行うかどうか（デフォルトはfalse）
     * trueの場合、両方のフィールドが必須となる
     */
    boolean required() default false;
}