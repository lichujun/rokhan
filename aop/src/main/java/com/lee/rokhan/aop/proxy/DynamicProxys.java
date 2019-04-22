package com.lee.rokhan.aop.proxy;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 获取各种动态代理的工厂方法类
 * @author lichujun
 * @date 2019/4/22 15:24
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicProxys {
    public static CglibProxy getCglibInstance() {
        return CglibProxyHolder.CGLIB_PROXY.getCglibProxy();
    }

    public static JdkProxy getJdkInstance() {
        return JdkProxyHolder.JDK_PROXY.getJdkProxy();
    }

    @Getter
    @AllArgsConstructor
    private enum CglibProxyHolder {
        CGLIB_PROXY(new CglibProxyImpl())
        ;
        private CglibProxy cglibProxy;
    }

    @Getter
    @AllArgsConstructor
    private enum JdkProxyHolder {
        JDK_PROXY(new JdkProxyImpl())
        ;
        private JdkProxy jdkProxy;
    }
}
