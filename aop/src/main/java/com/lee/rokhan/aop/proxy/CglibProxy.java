package com.lee.rokhan.aop.proxy;

import java.lang.reflect.Constructor;

/**
 * Cglib动态代理实例化对象接口
 * @author lichujun
 * @date 2019/4/22 15:06
 */
public interface CglibProxy {
    /**
     * Cglib动态代理实例化对象
     * @param tClass 需要被代理类的class对象
     * @param constructor 需要被代理类构造函数
     * @param args 需要被代理类构造函数的参数列表
     * @param <T>  需要被代理类的类型
     * @return 代理类的对象实例
     */
    <T> T instance(Class<T> tClass, Constructor<T> constructor, Object[] args);
}
