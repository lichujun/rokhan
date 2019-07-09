package com.lee.rokhan.vertx.web.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2018/12/13 11:28 PM
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ControllerInfo {

    /**
     * Controller类
     */
    private Class<?> controllerClass;

    /**
     * 执行的方法
     */
    private Method invokeMethod;

    /**
     * 方法的参数
     */
    private MethodParamsWithHeaders methodParamsWithHeaders;

    /**
     * Controller的Bean名称
     */
    private String beanName;
}
