package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.utils.AopProxyUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author lichujun
 * @date 2019/6/19 15:28
 */
@Slf4j
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    private final String beanName;
    private final Object target;
    private final List<Advisor> matchAdvisors;

    private BeanFactory beanFactory;

    public JdkDynamicAopProxy(String beanName, Object target, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        super();
        this.beanName = beanName;
        this.target = target;
        this.matchAdvisors = matchAdvisors;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return AopProxyUtils.applyAdvices(target, method, args, matchAdvisors, proxy, beanFactory);
    }

    @Override
    public Object getProxy() {
        return this.getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        if (log.isDebugEnabled()) {
            log.debug("为" + target + "创建代理。");
        }
        return Proxy.newProxyInstance(classLoader, target.getClass().getInterfaces(), this);
    }
}
