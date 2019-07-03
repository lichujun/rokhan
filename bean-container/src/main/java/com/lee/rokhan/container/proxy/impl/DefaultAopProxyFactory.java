package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.proxy.AopProxyFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Aop动态代理工厂类
 * 1、JDK动态代理
 * 2、Cglib动态代理
 * @author lichujun
 * @date 2019/6/19 15:50
 */
public class DefaultAopProxyFactory implements AopProxyFactory {

    /**
     * 通过类去匹配Bean对象
     */
    private final Set<String> classBeanNames = new HashSet<>();

    @Override
    public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        if (shouldUseJDKDynamicProxy(bean, beanName)) {
            return new JdkDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        } else {
            return new CglibDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
        }
    }

    private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
        if (classBeanNames.contains(beanName)) {
            return false;
        }
        return true;
    }

    public void addClassBeanName(String classBeanName) {
        classBeanNames.add(classBeanName);
    }
}
