package com.lee.rokhan.container.proxy;

import com.lee.rokhan.container.proxy.impl.DefaultAopProxyFactory;
import lombok.AllArgsConstructor;

/**
 * 获取Aop代理的工厂类
 * @author lichujun
 * @date 2019/6/25 10:51
 */
public class AopProxyFactories {

    /**
     * 获得默认的AopProxyFactory实例
     *
     * @return AopProxyFactory
     */
    public static AopProxyFactory getDefaultAopProxyFactory() {
        return AopProxyFactoryHolder.DEFAULT_FACTORY.aopProxyFactory;
    }

    /**
     * 使用枚举获取单例
     */
    @AllArgsConstructor
    enum AopProxyFactoryHolder {
        DEFAULT_FACTORY(new DefaultAopProxyFactory()),
        ;

        private AopProxyFactory aopProxyFactory;

    }
}
