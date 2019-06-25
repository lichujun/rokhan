package com.lee.rokhan.container.proxy;

/**
 * Aop代理
 * @author lichujun
 * @date 2019/6/18 17:43
 */
public interface AopProxy {

    /**
     * 获取代理对象
     * @return 代理对象
     * @throws Throwable 异常
     */
    Object getProxy() throws Throwable;

    /**
     * 获取代理对象
     * @param classLoader 类加载器
     * @return 代理对象
     * @throws Throwable 异常
     */
    Object getProxy(ClassLoader classLoader) throws Throwable;
}
