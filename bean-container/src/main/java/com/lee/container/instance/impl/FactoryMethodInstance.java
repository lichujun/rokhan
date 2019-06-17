package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.factory.BeanFactory;
import com.lee.container.instance.BeanInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 通过静态工厂方法生成Bean对象
 * @author lichujun
 * @date 2019/6/17 14:57
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class FactoryMethodInstance implements BeanInstance {

    /**
     * 通过静态工厂方法生成Bean对象
     * 注：暂只支持没有参数的静态方法
     * @param beanDefinition Bean注册信息
     * @return Bean对象
     * @throws Exception 异常
     */
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Exception {
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");
        Class<?> beanClass = beanDefinition.getBeanClass();
        Method method = beanClass.getDeclaredMethod(beanDefinition.getFactoryMethodName());
        return method.invoke(beanClass);
    }
}
