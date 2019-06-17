package com.lee.container.instance;

import com.lee.container.definition.BeanDefinition;

/**
 * Bean实例化
 * @author lichujun
 * @date 2019/6/17 14:28
 */
public interface BeanInstance {

    Object instance(BeanDefinition beanDefinition) throws Exception;
}
