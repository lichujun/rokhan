package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * 切入点
 * @author lichujun
 * @date 2019/6/25 15:54
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Pointcut {

    String value() default "";
}
