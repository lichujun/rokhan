package com.lee.rokhan.container.advice;

import java.lang.reflect.Method;

/**
 * 方法环绕通知
 * @author lichujun
 * @date 2019/6/18 14:24
 */
public interface MethodSurroudAdvice extends Advice {

    /**
     * 方法的环绕通知
     * @param method 方法
     * @param args 方法的参数列表对象
     * @param target 方法的目标对象
     * @return 方法执行后的返回值
     */
    Object invoke(Method method, Object[] args, Object target) throws Throwable;
}
