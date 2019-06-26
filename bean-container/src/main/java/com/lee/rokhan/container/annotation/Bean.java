package com.lee.rokhan.container.annotation;

/**
 * 工厂方法或Bean对象方法注册Bean
 * @author lichujun
 * @date 2019/6/25 16:55
 */
public @interface Bean {

    String value() default "";
}
