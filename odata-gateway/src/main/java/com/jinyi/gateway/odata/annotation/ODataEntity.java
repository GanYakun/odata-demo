package com.jinyi.gateway.odata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OData实体注解
 * 用于标记实体类，指定OData实体名称和对应的数据库表名
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataEntity {
    
    /**
     * OData实体名称
     */
    String name();
    
    /**
     * 对应的数据库表名
     */
    String table();
}