package com.lee.container.factory;

import com.lee.container.definition.BeanDefinition;

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
    Object getBeanByName(String beanName) throws Exception;

    /**
     * 通过类对象获取实例对象
     * @param classObject 类对象
     * @return 实例对象
     */
    Object getBeanByClass(Class<?> classObject) throws Exception;

    /**
     * 注册Bean信息
     * 不能存在相同的Bean名称
     * @param beanName Bean名称
     * @param beanDefinition Bean信息
     */
    void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws Exception;

    /**
     * 通过Bean名称获取注册Bean信息
     * @param beanName Bean名称
     * @return Bean信息
     */
    BeanDefinition getBeanDefinition(String beanName);

    /**
     * 获取构造函数的参数对象列表
     * @param beanDefinition Bean注册信息
     * @return 构造函数的参数对象列表
     * @throws Exception 异常
     */
    Object[] getConstructorArgumentValues(BeanDefinition beanDefinition) throws Exception;

    /**
     * 是否存在Bean名称的Bean信息
     * @param beanName Bean名称
     * @return Bean信息
     */
    boolean containsBeanDefinition(String beanName);
}
