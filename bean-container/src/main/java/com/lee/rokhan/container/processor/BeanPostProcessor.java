package com.lee.rokhan.container.processor;

/**
 * Bean初始化之前、之后所需要做的工作
 * @author lichujun
 * @date 2019/6/18 15:26
 */
public interface BeanPostProcessor {

    /**
     * Bean初始化之前做的工作
     */
    Object postProcessBeforeInitialization(String beanName, Object bean, Class<?> beanClass) throws Throwable;

    /**
     * Bean初始化完成后做的工作
     */
    Object postProcessAfterInitialization(String beanName, Object bean, Class<?> beanClass) throws Throwable;
}
