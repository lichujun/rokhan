package com.lee.rokhan.aop.proxy.handler;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * cglib动态代理Handler类
 * @author lichujun
 * @date 2019/4/22 14:52
 */
public class CglibMethodInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.println("do before");
        Object returnValue = proxy.invokeSuper(obj, args);
        System.out.println("do after");
        return returnValue;
    }
}
