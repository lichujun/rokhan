package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * Aop增强后置通知
 * @author lichujun
 * @date 2019/6/25 16:18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface After {
    String value() default "";
}
