package com.jinyi.odata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OData字段注解
 * 用于标记实体类的字段属性
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataField {
    
    /**
     * 字段名称，如果不指定则使用字段名
     */
    String name() default "";
    
    /**
     * 是否为主键
     */
    boolean key() default false;
    
    /**
     * 是否可为空
     */
    boolean nullable() default true;
    
    /**
     * 字符串字段的最大长度
     */
    int length() default 255;
}