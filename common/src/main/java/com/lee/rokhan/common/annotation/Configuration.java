package com.lee.rokhan.common.annotation;

import java.lang.annotation.*;

/**
 * 配置文件注解
 * @author lichujun
 * @date 2019/4/22 16:27
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {
    String value() default "";
}
