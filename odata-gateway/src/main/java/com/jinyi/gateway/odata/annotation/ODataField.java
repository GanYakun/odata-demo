package com.jinyi.gateway.odata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OData字段注解
 * 用于标记实体字段的OData属性
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataField {
    
    /**
     * 是否为主键
     */
    boolean key() default false;
    
    /**
     * 是否可为空
     */
    boolean nullable() default true;
    
    /**
     * 字段长度（用于字符串类型）
     */
    int length() default 255;
    
    /**
     * 字段描述
     */
    String description() default "";
}