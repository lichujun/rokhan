package com.lee.rokhan.container.factory;

import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.processor.BeanPostProcessor;

/**
 * 创建和获取Bean对象
 * @author lichujun
 * @date 2019/6/17 10:52
 */
public interface BeanFactory {

    /**
     * 通过Bean名称获取实例对象
     * 不能存在相同的Bean名称
     * @param beanName Bean名称
     * @return 实例对象
     */
    Object getBean(String beanName) throws Throwable;

    /**
     * 注册Bean信息
     * 不能存在相同的Bean名称
     * @param beanName Bean名称
     * @param beanDefinition Bean信息
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws Throwable;

    /**
     * 通过Bean名称获取注册Bean信息
     * @param beanName Bean名称
     * @return Bean信息
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * 是否存在Bean名称的Bean信息
     * @param beanName Bean名称
     * @return Bean信息
     */
    boolean containsBeanDefinition(String beanName);

    /**
     * 注册监视Bean的生命周期的对象
     * @param beanPostProcessor 监视Bean的生命周期的对象
     */
    void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
