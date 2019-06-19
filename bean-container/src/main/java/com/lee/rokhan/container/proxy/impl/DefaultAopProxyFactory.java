package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.proxy.AopProxyFactory;

import java.util.List;

/**
 * @author lichujun
 * @date 2019/6/19 15:50
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

    @Override
    public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory)
            throws Throwable {
        // 是该用jdk动态代理还是cglib？
        if (shouldUseJDKDynamicProxy(bean, beanName)) {
            return new JdkDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        } else {
            return new CglibDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        }
    }

    private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
        // TODO 如何判断？有实现接口就用JDK,没有就用cglib？
        return false;
    }
}
