package com.lee.rokhan.container.instance.impl;

import com.lee.rokhan.container.instance.BeanInstance;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
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
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class FactoryMethodInstance extends IocDetermine implements BeanInstance {

    /**
     * 通过静态工厂方法生成Bean对象
     * @param beanDefinition Bean注册信息
     * @return Bean对象
     * @throws Throwable 异常
     */
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Throwable {
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object[] args = beanDefinition.getArgumentRealValues(beanFactory);
        Method method = determineMethod(beanDefinition, args, beanClass);
        method.setAccessible(true);
        return method.invoke(beanClass, args);
    }
}
