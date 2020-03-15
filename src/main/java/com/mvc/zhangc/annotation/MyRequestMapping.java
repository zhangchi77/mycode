package com.mvc.zhangc.annotation;


import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD}) //只能在类上使用
@Retention(RetentionPolicy.RUNTIME) //表示自运行时可以通过反射获取载体
@Documented //javadoc 载体
public @interface MyRequestMapping {
    String value() default "";
}
