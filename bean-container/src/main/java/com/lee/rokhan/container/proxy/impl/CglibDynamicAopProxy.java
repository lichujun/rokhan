package com.lee.rokhan.container.proxy.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.proxy.AopProxy;
import com.lee.rokhan.container.utils.AopProxyUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 通过Cglib生成代理对象
 * @author lichujun
 * @date 2019/6/19 15:30
 */
@Slf4j
@AllArgsConstructor
public class CglibDynamicAopProxy implements AopProxy, MethodInterceptor {

    private static final Enhancer ENHANCER = new Enhancer();

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

