package com.lee.rokhan.container.instance;

import com.lee.rokhan.container.definition.BeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * 确认方法的抽象方法
 * @author lichujun
 * @date 2019/6/18 10:18
 */
public interface Determine {

    /**
     * 通过Bean注册信息和构造方法的参数列表对象确认构造方法
     * @param beanDefinition Bean注册信息
     * @param args 构造方法的参数列表对象
     * @return 构造方法
     * @throws Throwable 异常
     */
    Constructor determineConstructor(BeanDefinition beanDefinition, Object[] args) throws Throwable;

    /**
     * 通过Bean注册信息、方法的参数列表对象和方法的类对象确认方法
     * @param beanDefinition Bean注册信息
     * @param args 方法的参数列表对象
     * @param type 方法的类对象
     * @return 方法
     * @throws Throwable 异常
     */
    Method determineMethod(BeanDefinition beanDefinition, Object[] args, Class<?> type) throws Throwable;

    /**
     * 通过参数对象数组获取类对象数组
     * @param args 参数对象数组
     * @return 类对象数组
     */
    default Class<?>[] getObjectsClass(Object[] args) {
        Class<?>[] paramTypes = new Class[args.length];
        int j = 0;
        for (Object p : args) {
            paramTypes[j++] = p.getClass();
        }
        return paramTypes;
    }

    /**
     * 比较参数列表对象的类型是否能匹配
     * @param args 参数列表对象
     * @param parameterTypes 参数列表类型
     * @return 是否能匹配
     */
    default boolean compareParameterType(Object[] args, Class<?>[] parameterTypes) {
        if (parameterTypes.length == args.length) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
