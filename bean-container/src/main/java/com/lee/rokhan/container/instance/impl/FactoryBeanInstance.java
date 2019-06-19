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
 * 通过工厂Bean的方法创建对象
 *
 * @author lichujun
 * @date 2019/6/17 14:58
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class FactoryBeanInstance extends IocMethodDetermine implements BeanInstance {

    /**
     * 通过工厂Bean的方法创建对象
     *
     * @param beanDefinition Bean注册信息
     * @return Bean对象
     * @throws Throwable 异常
     */
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Throwable {
        // 校验参数
        Objects.requireNonNull(beanDefinition.getFactoryBeanName(), "工厂Bean名称factoryBeanName不能为空");
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");

        String factoryBeanName = beanDefinition.getFactoryBeanName();
        Object factoryBean = beanFactory.getBean(factoryBeanName);
        Objects.requireNonNull(factoryBean, "工厂Bean：factoryBean不能为空");

        Object[] args = beanDefinition.getArgumentRealValues(beanFactory);
        Class<?> factoryBeanClass = factoryBean.getClass();
        Method method = determineMethod(beanDefinition, args, factoryBeanClass);
        return method.invoke(factoryBean, args);
    }

}
