package com.lee.rokhan.container.proxy;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;

import java.util.List;

/**
 * Aop代理工厂
 * @author lichujun
 * @date 2019/6/19 15:50
 */
public interface AopProxyFactory {

    /**
     * 获取Aop代理，进行方法增强
     * @param bean Bean对象
     * @param beanName Bean名称
     * @param matchAdvisors 匹配到的所有增强器
     * @param beanFactory Bean工厂
     * @return Aop代理：1、JDK动态代理 2、JDK动态代理 ...
     * @throws Throwable 异常
     */
    AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory)
            throws Throwable;
}
