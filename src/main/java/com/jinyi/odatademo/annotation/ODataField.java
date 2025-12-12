package com.jinyi.odatademo.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ODataField {
    String name() default "";
    boolean key() default false;
    boolean nullable() default true;
    int length() default 255;
    String type() default "";
}