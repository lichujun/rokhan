package com.lee.rokhan.aop.proxy;

/**
 * Jdk动态代理实例化对象接口
 * @author lichujun
 * @date 2019/4/22 14:36
 */
public interface JdkProxy {

    /**
     * Jdk动态代理实例化对象
     * @param interfaceClass 需要被代理类实现的接口
     * @param realObject 需要被代理类原本实例化的对象
     * @param <T> 需要被代理类的类型
     * @return 代理类的对象实例
     */
    <T> T instance(Class<T> interfaceClass, Object realObject);
}
