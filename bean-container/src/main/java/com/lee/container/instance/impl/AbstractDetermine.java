package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.instance.Determine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/6/18 10:23
 */
public abstract class AbstractDetermine implements Determine {

    @Override
    public Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Exception {
        throw new UnsupportedOperationException("不支持该方法");
    }

    @Override
    public Method determineMethod(BeanDefinition beanDefinition, Object[] args, Class<?> type) throws Exception {
        throw new UnsupportedOperationException("不支持该方法");
    }
}
