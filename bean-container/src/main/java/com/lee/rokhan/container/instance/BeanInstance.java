package com.lee.rokhan.container.instance;

import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;

/**
 * Bean实例化 1、构造方法 2、静态方法 3、对象方法
 * @author lichujun
 * @date 2019/6/17 14:28
 */
public interface BeanInstance {

    Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Throwable;
}
