package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * 注入bean的注解
 * @author lichujun
 * @date 2019/4/22 15:50
 */

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
    String value() default "";
}
