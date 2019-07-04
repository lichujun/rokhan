package com.lee.rokhan.container.instance.impl;

import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.instance.BeanInstance;
import lombok.AllArgsConstructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 动态代理实例化对象
 * @author lichujun
 * @date 2019/7/4 17:34
 */
@AllArgsConstructor
public class JdkDynamicProxyInstance implements BeanInstance {

    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) {
        Class<?>[] interfaces = beanDefinition.getInterfaces();
        InvocationHandler invocationHandler = beanDefinition.getInvocationHandler();
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces ,invocationHandler);
    }
}
