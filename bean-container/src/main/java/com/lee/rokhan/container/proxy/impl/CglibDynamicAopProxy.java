package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.utils.AopProxyUtils;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author lichujun
 * @date 2019/6/19 15:30
 */
@Slf4j
public class CglibDynamicAopProxy implements AopProxy, MethodInterceptor {

    private static final Enhancer ENHANCER = new Enhancer();

    private final String beanName;
    private final Object target;

    private final List<Advisor> matchAdvisors;

    private final BeanFactory beanFactory;

    public CglibDynamicAopProxy(String beanName, Object target, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
        super();
        this.beanName = beanName;
        this.target = target;
        this.matchAdvisors = matchAdvisors;
        this.beanFactory = beanFactory;
    }

    @Override
    public Object getProxy() throws Throwable {
        return this.getProxy(target.getClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("为" + target + "创建cglib代理。");
        }
        Class<?> superClass = this.target.getClass();
        ENHANCER.setSuperclass(superClass);
        ENHANCER.setInterfaces(this.getClass().getInterfaces());
        ENHANCER.setCallback(this);
        Constructor<?> constructor = null;
        try {
            constructor = superClass.getDeclaredConstructor();
        } catch (NoSuchMethodException | SecurityException e) {
            log.warn("获取构造方法发生异常");
        }
        if (constructor != null) {
            return ENHANCER.create();
        } else {
            BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
            return ENHANCER.create(bd.getConstructor().getParameterTypes(), bd.getArgumentRealValues(beanFactory));
        }
    }

    @Override
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return AopProxyUtils.applyAdvices(target, method, args, matchAdvisors, proxy, beanFactory);
    }

}

