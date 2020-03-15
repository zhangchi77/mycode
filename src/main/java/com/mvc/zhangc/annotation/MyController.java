package com.mvc.zhangc.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE) //只能在类上使用
@Retention(RetentionPolicy.RUNTIME) //表示自运行时可以通过反射获取载体
@Documented  //载体
public @interface MyController {
    String value() default "";
}
