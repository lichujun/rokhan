package com.lee.rokhan.container.advice;

import java.lang.reflect.Method;

/**
 * 方法前置增强接口
 * @author lichujun
 * @date 2019/6/18 14:19
 */
public interface MethodBeforeAdvice extends Advice {

    /**
     * 实现方法的前置增强
     * @param method 方法
     * @param args 方法的参数列表对象
     * @param target 方法的目标对象
     */
    void before(Method method, Object[] args, Object target) throws Throwable;
}
