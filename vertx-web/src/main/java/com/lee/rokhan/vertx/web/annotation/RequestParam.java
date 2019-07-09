package com.lee.rokhan.vertx.web.annotation;

import java.lang.annotation.*;

/**
 * 请求方法的参数
 * @author lichujun
 * @date 2018/12/13 11:14 PM
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    /**
     * 请求方法的参数名
     */
    String value() default "";

    /**
     * 是否必传
     */
    boolean isRequired() default true;
}
