package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * 工厂方法或Bean对象方法注册Bean
 * @author lichujun
 * @date 2019/6/25 16:55
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    String value() default "";

    String initMethod() default "";

    String destroyMethod() default "";
}
