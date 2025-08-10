package com.example.todoapp.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * カスタムバリデーションアノテーション - 過去日付チェック
 * 指定された日付が過去でないことを確認する（今日は含む）
 * 
 * @author System
 */
@Documented
@Constraint(validatedBy = NotPastDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotPastDate {
    
    String message() default "過去の日付は指定できません";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 今日の日付を含むかどうか（デフォルトはtrue）
     * falseの場合、今日より後の日付のみ有効
     */
    boolean includeToday() default true;
}