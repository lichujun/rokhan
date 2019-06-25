package com.lee.rokhan.container.instance.impl;

import com.lee.rokhan.container.definition.BeanDefinition;

import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/6/18 10:29
 */
public class IocMethodDetermine extends AbstractDetermine {

    @Override
    public Method determineMethod(BeanDefinition beanDefinition, Object[] args, Class<?> type) throws Throwable {
        if (type == null) {
            type = beanDefinition.getBeanClass();
        }
        String methodName = beanDefinition.getFactoryMethodName();
        if (args == null) {
            return type.getMethod(methodName);
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
