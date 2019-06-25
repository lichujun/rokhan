package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * 数据库交互
 * @author lichujun
 * @date 2019/6/25 16:52
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Repository {
    String value() default "";
}
