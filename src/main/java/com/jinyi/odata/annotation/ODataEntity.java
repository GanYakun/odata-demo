package com.jinyi.odata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OData实体注解
 * 用于标记一个类为OData实体
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataEntity {
    
    /**
     * 实体名称，用于OData服务中的实体集名称
     */
    String name();
    
    /**
     * 对应的数据库表名，如果不指定则使用实体名称的小写形式
     */
    String table() default "";
}