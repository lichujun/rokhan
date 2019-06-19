package com.lee.rokhan.container.advice;

import java.lang.reflect.Method;

/**
 * 方法后置增强
 * @author lichujun
 * @date 2019/6/18 14:22
 */
public interface MethodReturnAdvice extends Advice {

    /**
     * 方法的后置增强
     * @param returnValue 方法执行后的返回值
     * @param method 方法
     * @param args 方法的参数列表对象
     * @param target 方法的目标对象
     */
    void afterReturn(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
}
