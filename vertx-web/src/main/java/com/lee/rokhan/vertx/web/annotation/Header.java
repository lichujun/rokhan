package com.lee.rokhan.vertx.web.annotation;

import java.lang.annotation.*;

/**
 * Http头信息
 * @author lichujun
 * @date 2019/6/12 15:08
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Header {
}
