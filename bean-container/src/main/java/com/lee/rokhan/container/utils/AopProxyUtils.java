package com.lee.rokhan.container.utils;

import com.lee.rokhan.container.advice.AopAdviceChainInvocation;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.advisor.impl.AspectJPointcutAdvisor;
import com.lee.rokhan.container.factory.BeanFactory;
import org.apache.commons.collections4.CollectionUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Aop代理的工具类
 * 进行方法增强
 * @author lichujun
 * @date 2019/6/19 15:25
 */
public class AopProxyUtils {

    /**
     * 对方法进行增强
     * @param target 目标Bean对象
     * @param method 目标Bean对象的方法
     * @param args 目标Bean对象的方法的参数
     * @param matchAdvisors 匹配到的增强器
     * @param proxy 目标Bean对象的代理对象
     * @param beanFactory Bean工厂
     * @return Bean对象的代理对象
     * @throws Throwable 异常
     */
    public static Object applyAdvices(Object target, Method method, Object[] args,
                                      List<Advisor> matchAdvisors, Object proxy, BeanFactory beanFactory) throws Throwable {
        // 1、获取要对当前方法进行增强的advice
        List<Object> advices = AopProxyUtils.getShouldApplyAdvices(target, method, matchAdvisors,
                beanFactory);
        // 2、如有增强的advice，责任链式增强执行
        if (CollectionUtils.isEmpty(advices)) {
            return method.invoke(target, args);
        } else {
            // 责任链式执行增强
            AopAdviceChainInvocation chain = new AopAdviceChainInvocation(proxy, target, method, args, advices);
            return chain.invoke();
        }
    }

    /**
     * 目标Bean对象的方法需要进行通知的所有Advice
     * @param target 目标Bean对象
     * @param method 目标Bean对象的方法
     * @param matchAdvisors 匹配到的所有增强器
     * @param beanFactory Bean工厂
     * @return 需要进行通知的所有Advice
     * @throws Throwable 异常
     */
    private static List<Object> getShouldApplyAdvices(Object target, Method method, List<Advisor> matchAdvisors,
                                                      BeanFactory beanFactory) throws Throwable {
        if (CollectionUtils.isEmpty(matchAdvisors)) {
            return null;
        }
        List<Object> advices = new ArrayList<>();
        for (Advisor ad : matchAdvisors) {
            if (ad instanceof AspectJPointcutAdvisor) {
                if (ad.getPointcut().matchMethod(method)) {
                    advices.add(beanFactory.getBean(ad.getAdviceBeanName()));
                }
            }
        }
        return advices;
    }
}
