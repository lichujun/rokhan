package com.lee.rokhan.container.proxy;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.impl.DefaultAopProxyFactory;

import java.util.List;

/**
 * @author lichujun
 * @date 2019/6/19 15:50
 */
public interface AopProxyFactory {

    AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory)
            throws Throwable;

    /**
     * 获得默认的AopProxyFactory实例
     *
     * @return AopProxyFactory
     */
    static AopProxyFactory getDefaultAopProxyFactory() {
        return new DefaultAopProxyFactory();
    }
}
