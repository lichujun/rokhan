package com.lee.rokhan.aop.proxy;

import com.lee.rokhan.aop.proxy.handler.CglibMethodInterceptor;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.sf.cglib.proxy.Enhancer;
import java.lang.reflect.Constructor;

/**
 * Cglib动态代理实现类
 * @author lichujun
 * @date 2019/4/22 14:58
 */
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CglibProxyImpl implements CglibProxy {

    public <T> T instance(Class<T> tClass, Constructor<T> constructor, Object[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(tClass);
        enhancer.setCallback(new CglibMethodInterceptor());
        if (constructor == null) {
            return tClass.cast(enhancer.create());
        } else {
            return tClass.cast(enhancer.create(constructor.getParameterTypes(), args));
        }
    }
}
