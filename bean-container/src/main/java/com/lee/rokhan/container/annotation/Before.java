package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * Aop增强前置通知
 * @author lichujun
 * @date 2019/6/25 15:58
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Before {

    String value() default "";
}
