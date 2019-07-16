package com.lee.rokhan.demo.annotation;

import java.lang.annotation.*;

/**
 * @author lichujun
 * @date 2019/7/16 15:44
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AopTest {
}
