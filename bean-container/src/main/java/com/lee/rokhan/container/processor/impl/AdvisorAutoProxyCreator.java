package com.lee.rokhan.container.processor.impl;

import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.advisor.impl.AspectJPointcutAdvisor;
import com.lee.rokhan.container.pointcut.Pointcut;
import com.lee.rokhan.container.processor.BeanPostProcessor;
import com.lee.rokhan.container.proxy.AopProxyFactory;
import org.apache.commons.collections4.CollectionUtils;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author lichujun
 * @date 2019/6/18 16:58
 */
public class AdvisorAutoProxyCreator implements BeanPostProcessor {

    private List<Advisor> advisors;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Throwable {
        // 第一步：在此判断bean是否需要进行切面增强
        List<Advisor> matchAdvisors = getMatchedAdvisors(bean, beanName);

        // 第二步：如需要就进行增强,创建代理对象，进行代理增强。再返回增强的对象。
        if (CollectionUtils.isNotEmpty(matchAdvisors)) {
            bean = this.createProxy(bean, beanName, matchAdvisors);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Throwable {
        // 在此判断bean是否需要进行切面增强
        List<Advisor> matchAdvisors = getMatchedAdvisors(bean, beanName);
        // 如需要就进行增强,再返回增强的对象。
        if (CollectionUtils.isNotEmpty(matchAdvisors)) {
            bean = this.createProxy(bean, beanName, matchAdvisors);
        }
        return bean;
    }

    private List<Advisor> getMatchedAdvisors(Object bean, String beanName) {
        if (CollectionUtils.isEmpty(advisors)) {
            return null;
        }

        // 得到类、所有的方法
        Class<?> beanClass = bean.getClass();
        Set<Method> allMethods = ReflectionUtils.getDeclaredMethods(beanClass);

        // 存放匹配的Advisor的list
        List<Advisor> matchAdvisors = new ArrayList<>();
        // 遍历Advisor来找匹配的
        for (Advisor ad : advisors) {
            if (ad instanceof AspectJPointcutAdvisor) {
                if (isPointcutMatchBean(ad, beanClass, allMethods)) {
                    matchAdvisors.add(ad);
                }
            }
        }
        return matchAdvisors;
    }

    private boolean isPointcutMatchBean(Advisor advisor, Class<?> beanClass, Set<Method> methods) {
        Pointcut p = advisor.getPointcut();

        // 首先判断类是否匹配
        // 注意之前说过的AspectJ情况下这个匹配是不可靠的，需要通过方法来匹配
        //这里的判断仅仅起到过滤作用，类不匹配的前提下直接跳过
        if (!p.matchClass(beanClass)) {
            return false;
        }

        // 再判断是否有方法匹配
        for (Method method : methods) {
            if (p.matchMethod(method)) {
                return true;
            }
        }
        return false;
    }

    private Object createProxy(Object bean, String beanName, List<Advisor> matchAdvisors) throws Throwable {
        // 通过AopProxyFactory工厂去完成选择、和创建代理对象的工作。
        /*return AopProxyFactory.getDefaultAopProxyFactory().createAopProxy(bean, beanName, matchAdvisors, beanFactory)
                .getProxy();*/
        return null;
    }

}
