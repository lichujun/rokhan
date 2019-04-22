package com.lee.rokhan.common.annotation;

import java.lang.annotation.*;

/**
 * Controller层注解，标记该类为控制器，value代表注入bean的名称
 * @author lichujun
 * @date 2019/4/22 15:42
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {

    String value() default "";
}
