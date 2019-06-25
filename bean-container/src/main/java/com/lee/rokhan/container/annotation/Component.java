package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * 组件注解
 * @author lichujun
 * @date 2019/4/22 16:21
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    String value() default "";
}
