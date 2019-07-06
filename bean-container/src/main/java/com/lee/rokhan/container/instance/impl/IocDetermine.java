package com.lee.rokhan.container.instance.impl;

import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.container.definition.BeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Ioc确认构造方法
 * @author lichujun
 * @date 2019/6/18 10:29
 */
public class IocDetermine extends AbstractDetermine {

    @Override
    public Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Throwable {
        Constructor constructor;
        Class<?> beanClass = beanDefinition.getBeanClass();
        //当没有任何一个参数时直接获取无参构造方法
        if (args == null) {
            return beanClass.getConstructor();
        }
        //对于原型bean,第二次开始获取Bean实例时,可直接获取第一次缓存的构造方法
        constructor = beanDefinition.getConstructor();
        if (constructor != null) {
            return constructor;
        }
        //根据参数类型获取精确匹配的构造方法
        Class[] paramTypes = getObjectsClass(args);
        try {
            constructor = beanClass.getConstructor(paramTypes);
        } catch (Throwable e) {
            // 此异常不需要进行处理
        }

        if (constructor == null) {
            //把所有的构造器全部遍历出来一一比对
            for (Constructor<?> allConstructor : beanClass.getConstructors()) {
                Class<?>[] pTypes = allConstructor.getParameterTypes();
                if (compareParameterType(args, pTypes)) {
                    constructor = allConstructor;
                    break;
                }
            }
        }

        if (constructor != null) {
            if (beanDefinition.isPrototype()) {
                //对原型bean构造器进行缓存方便下次查找
                beanDefinition.setConstructor(constructor);
            }
            return constructor;
        } else {
            throw new RuntimeException("不存在对应的构造方法!" + beanDefinition);
        }
    }

    @Override
    public Method determineMethod(BeanDefinition beanDefinition, Object[] args, Class<?> type) throws Throwable {
        if (type == null) {
            type = beanDefinition.getBeanClass();
        }
        String methodName = beanDefinition.getFactoryMethodName();
        if (args == null) {
            return ReflectionUtils.getDeclaredMethod(type, methodName);
        }
        Method method;
        // 对于原型bean,从第二次开始获取bean实例时，可直接获得第一次缓存的构造方法。
        method = beanDefinition.getFactoryMethod();
        if (method != null) {
            return method;
        }
        // 根据参数类型获取精确匹配的方法
        Class[] paramTypes = getObjectsClass(args);
        try {
            method = type.getMethod(methodName, paramTypes);
        } catch (Throwable e) {
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
                if (compareParameterType(args, parameterTypes)) {
                    method = m0;
                    break;
                }
            }
        }
        if (method != null) {
            // 对于原型bean,可以缓存找到的方法，方便下次构造实例对象。在BeanDefinition中获取设置所用方法的方法。
            if (beanDefinition.isPrototype()) {
                beanDefinition.setFactoryMethod(method);
            }
            return method;
        } else {
            throw new RuntimeException("不存在对应的方法！" + beanDefinition);
        }
    }
}
