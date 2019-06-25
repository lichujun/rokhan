package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * Aop增强环绕通知
 * @author lichujun
 * @date 2019/6/25 16:15
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Around {
    String value() default "";
}
