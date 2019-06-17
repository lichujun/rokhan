package com.lee.container.instance;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.factory.BeanFactory;

/**
 * Bean实例化
 * @author lichujun
 * @date 2019/6/17 14:28
 */
public interface BeanInstance {

    Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Exception;

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

    default boolean compareParameterType(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }
}
