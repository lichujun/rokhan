package com.lee.rokhan.aop.proxy;

import com.lee.rokhan.aop.proxy.handler.JdkInvocationHandlerImpl;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * jdk动态代理实现类
 * @author lichujun
 * @date 2019/4/22 14:39
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class JdkProxyImpl implements JdkProxy {

    public <T> T instance(Class<T> interfaceClass, Object realObject) {
        InvocationHandler invocationHandler = new JdkInvocationHandlerImpl(realObject);
        ClassLoader loader = JdkInvocationHandlerImpl.class.getClassLoader();
        Class[] interfaces = {interfaceClass};
        Object proxy = Proxy.newProxyInstance(loader, interfaces, invocationHandler);
        return interfaceClass.cast(proxy);
    }
}
