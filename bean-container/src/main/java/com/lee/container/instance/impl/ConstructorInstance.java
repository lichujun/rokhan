package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.factory.BeanFactory;
import com.lee.container.instance.BeanInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;

/**
 * @author lichujun
 * @date 2019/6/17 15:01
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ConstructorInstance implements BeanInstance {

    /**
     * 通过构造函数实例化Bean对象
     *
     * @param beanDefinition Bean注册信息
     * @return Bean对象
     * @throws Exception 异常
     */
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Exception {
        try {
            Object[] args = beanFactory.getConstructorArgumentValues(beanDefinition);
            if (args == null) {
                return beanDefinition.getBeanClass().newInstance();
            } else {
                Constructor constructor = determineConstructor(beanDefinition, args);
                return constructor.newInstance(args);
            }
        } catch (SecurityException e) {
            log.error("创建bean的实例异常,beanDefinition" + beanDefinition, e);
            throw e;
        }
    }

    /**
     * 通过Bean注册信息和构造函数的参数列表获取构造函数
     *
     * @param beanDefinition Bean注册信息
     * @param args 构造函数的参数列表
     * @return 构造函数
     * @throws Exception 异常
     */
    private Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Exception {
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
                //此构造方法的参数长度等于提供参数长度
                if (pTypes.length == args.length) {
                    if (compareParameterType(paramTypes, args)) {
                        constructor = allConstructor;
                        break;
                    }
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
