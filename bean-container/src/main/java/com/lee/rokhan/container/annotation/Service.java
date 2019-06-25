package com.lee.rokhan.container.annotation;

import java.lang.annotation.*;

/**
 * Service注解，标记的类只做业务处理，value代表注入bean的名称
 * @author lichujun
 * @date 2019/4/22 15:46
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {

    String value() default "";
}
