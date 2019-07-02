package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * 切面
 * @author lichujun
 * @date 2019/6/26 10:13
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Aspect {
}
