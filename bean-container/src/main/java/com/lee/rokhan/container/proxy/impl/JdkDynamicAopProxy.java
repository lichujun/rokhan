package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.utils.AopProxyUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

/**
 * 通过JDK生成代理对象
 * @author lichujun
 * @date 2019/6/19 15:28
 */
@Slf4j
@AllArgsConstructor
public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

    /**
     * Bean名称
     */
    private final String beanName;
    /**
     * Bean对象
     */
    private final Object target;
    /**
     * 匹配到的增强器
     */
    private final List<Advisor> matchAdvisors;
    /**
     * Bean工厂
     */
    private final BeanFactory beanFactory;

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
        Set<Class<?>> interfaceSet = ReflectionUtils.getInterfaces(target.getClass());
        Class<?>[] interfaces = interfaceSet.toArray(new Class[0]);
        return Proxy.newProxyInstance(classLoader, interfaces, this);
    }
}
