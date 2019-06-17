package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.factory.impl.BeanFactories;
import com.lee.container.instance.BeanInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 通过工厂Bean的方法创建对象
 *
 * @author lichujun
 * @date 2019/6/17 14:58
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class FactoryBeanInstance implements BeanInstance {

    /**
     * 通过工厂Bean的方法创建对象
     * 注：暂只支持没有参数的静态方法
     *
     * @param beanDefinition Bean注册信息
     * @return Bean对象
     * @throws Exception 异常
     */
    @Override
    public Object instance(BeanDefinition beanDefinition) throws Exception {
        // 校验参数
        Objects.requireNonNull(beanDefinition.getFactoryBeanName(), "工厂Bean名称factoryBeanName不能为空");
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");

        String factoryBeanName = beanDefinition.getFactoryBeanName();
        Object factoryBean = BeanFactories.getIocBeanFactory().getBeanByName(factoryBeanName);
        Objects.requireNonNull(factoryBean, "工厂Bean：factoryBean不能为空");

        Class<?> factoryBeanClass = factoryBean.getClass();
        Method method = factoryBeanClass.getDeclaredMethod(beanDefinition.getFactoryMethodName());
        return method.invoke(factoryBean);
    }
}
