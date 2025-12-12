package com.jinyi.odatademo.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataEntity {
    String name() default "";
    String table() default "";
    boolean autoCreate() default true;
}
