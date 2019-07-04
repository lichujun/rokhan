package com.lee.rokhan.demo.context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/7/4 19:06
 */
public class MapperInvocationHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MapperInterface mapperInterface = () -> "hello mapper";
        System.out.println("你来了");
        return method.invoke(mapperInterface, args);
    }
}
