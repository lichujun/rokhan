package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.factory.BeanFactory;
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
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Exception {
        // 校验参数
        Objects.requireNonNull(beanDefinition.getFactoryBeanName(), "工厂Bean名称factoryBeanName不能为空");
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");

        String factoryBeanName = beanDefinition.getFactoryBeanName();
        Object factoryBean = beanFactory.getBeanByName(factoryBeanName);
        Objects.requireNonNull(factoryBean, "工厂Bean：factoryBean不能为空");

        Class<?> factoryBeanClass = factoryBean.getClass();
        Method method = factoryBeanClass.getDeclaredMethod(beanDefinition.getFactoryMethodName());
        return method.invoke(factoryBean);
    }

    private Method determineFactoryMethod(BeanDefinition beanDefinition, Object[] args, Class<?> type) throws Exception {
        if (type == null) {
            type = beanDefinition.getBeanClass();
        }
        String methodName = beanDefinition.getFactoryMethodName();
        if (args == null) {
            return type.getMethod(methodName);
        }
        Method method = null;
        // 对于原型bean,从第二次开始获取bean实例时，可直接获得第一次缓存的构造方法。
        method = beanDefinition.getFactoryMethod();
        if (method != null) {
            return method;
        }
        // 根据参数类型获取精确匹配的方法
        Class[] paramTypes = getObjectsClass(args);
        try {
            method = type.getMethod(methodName, paramTypes);
        } catch (Exception e) {
            // 这个异常不需要处理
        }
        if (method == null) {
            // 没有精确参数类型匹配的，则遍历匹配所有的方法
            // 判断逻辑：先判断参数数量，再依次比对形参类型与实参类型
            for (Method m0 : type.getMethods()) {
                if (!m0.getName().equals(methodName)) {
                    continue;
                }
                Class<?>[] parameterTypes = m0.getParameterTypes();
                if (parameterTypes.length == args.length) {
                    if (compareParameterType(paramTypes, args)) {
                        method = m0;
                        break;
                    }
                }
            }
        }
        if (method != null) {
            // 对于原型bean,可以缓存找到的方法，方便下次构造实例对象。在BeanDefinfition中获取设置所用方法的方法。
            if (beanDefinition.isPrototype()) {
                beanDefinition.setFactoryMethod(method);
            }
            return method;
        } else {
            throw new Exception("不存在对应的方法！" + beanDefinition);
        }
    }

}
