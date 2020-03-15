package com.mvc.zhangc.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD) //只能在类的成员变量上使用
@Retention(RetentionPolicy.RUNTIME) //表示自运行时可以通过反射获取载体
@Documented //载体
public @interface MyAutowired {
    String value() default "";
}
