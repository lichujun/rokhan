package com.lee.rokhan.aop.proxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * jdk动态代理Handler实现类
 * @author lichujun
 * @date 2019/4/22 14:33
 */
public class JdkInvocationHandlerImpl implements InvocationHandler {
    
    private Object subject;
    
    public JdkInvocationHandlerImpl(Object subject) {
        this.subject = subject;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("jdk动态代理");
        Object returnValue = null;
        if (subject != null) {
            returnValue = method.invoke(subject, args);
        }
        return returnValue;
    }
}
