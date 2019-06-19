package com.lee.rokhan.container.proxy;

/**
 * 获取Aop代理对象
 * @author lichujun
 * @date 2019/6/18 17:43
 */
public interface AopProxy {

    Object getProxy() throws Throwable;

    Object getProxy(ClassLoader classLoader) throws Throwable;
}
