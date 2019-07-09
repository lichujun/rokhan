package com.lee.rokhan.vertx.web.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Type;

/**
 * @author lichujun
 * @date 2019/3/15 11:49 PM
 */
@Data
@AllArgsConstructor
public class MethodParam {

    private Class<?> paramClass;

    private Type type;
}
