package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.proxy.AopProxyFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Aop动态代理
 * 1、JDK动态代理
 * 2、Cglib动态代理
 * @author lichujun
 * @date 2019/6/19 15:50
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

    @Override
    public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory)
            throws Throwable {
        if (shouldUseJDKDynamicProxy(bean, beanName)) {
            return new JdkDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        } else {
            return new CglibDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        }
    }

    private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
        if (bean == null) {
            return false;
        }
        Class<?> beanClass = bean.getClass();
        return !ArrayUtils.isEmpty(beanClass.getInterfaces());
    }
}
