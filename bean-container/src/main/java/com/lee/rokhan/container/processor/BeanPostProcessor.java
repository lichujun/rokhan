package com.lee.rokhan.container.processor;

/**
 * 监听Bean的生命周期
 * @author lichujun
 * @date 2019/6/18 15:26
 */
public interface BeanPostProcessor {

    /**
     * Bean初始化之前做的操作
     */
    Object postProcessBeforeInitialization(Object bean, String beanName) throws Throwable;

    /**
     * Bean初始化完成后做的操作
     */
    Object postProcessAfterInitialization(Object bean, String beanName) throws Throwable;
}
