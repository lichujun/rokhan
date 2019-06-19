package com.lee.rokhan.container.instance.impl;

import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.instance.Determine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/6/18 10:23
 */
public abstract class AbstractDetermine implements Determine {

    @Override
    public Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Throwable {
        throw new UnsupportedOperationException("不支持该方法");
    }

    @Override
    public Method determineMethod(BeanDefinition beanDefinition, Object[] args, Class<?> type) throws Throwable {
        throw new UnsupportedOperationException("不支持该方法");
    }
}
