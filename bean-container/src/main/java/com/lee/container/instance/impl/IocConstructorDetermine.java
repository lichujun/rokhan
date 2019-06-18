package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;

import java.lang.reflect.Constructor;

/**
 * Ioc确认构造方法
 * @author lichujun
 * @date 2019/6/18 10:29
 */
public class IocConstructorDetermine extends AbstractDetermine {

    @Override
    public Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Exception {
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
        } catch (Exception e) {
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
            throw new Exception("不存在对应的构造方法!" + beanDefinition);
        }
    }
}
