package com.example.todoapp.validation;

import java.lang.annotation.*;

/**
 * ValidDateRangeアノテーションのコンテナアノテーション
 * 複数のValidDateRangeを同一のクラスに適用するために使用
 * 
 * @author System
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRanges {
    ValidDateRange[] value();
}