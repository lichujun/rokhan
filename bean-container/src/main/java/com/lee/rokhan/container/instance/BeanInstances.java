package com.lee.rokhan.container.instance;

import com.lee.rokhan.container.instance.impl.ConstructorInstance;
import com.lee.rokhan.container.instance.impl.FactoryBeanMethodInstance;
import com.lee.rokhan.container.instance.impl.FactoryMethodInstance;
import com.lee.rokhan.container.instance.impl.JdkDynamicProxyInstance;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 实例生成器的工厂方法
 * @author lichujun
 * @date 2019/6/17 15:10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanInstances {

    /**
     * 获取构造函数生成对象的实例生成器
     * @return 构造函数生成对象的实例生成器
     */
    public static BeanInstance getConstructorInstance() {
        return BeanInstanceHolder.CONSTRUCTOR_INSTANCE.beanInstance;
    }

    /**
     * 获取工厂Bean生成对象的实例生成器
     * @return 工厂Bean生成对象的实例生成器
     */
    public static BeanInstance getFactoryBeanInstance() {
        return BeanInstanceHolder.FACTORY_BEAN_INSTANCE.beanInstance;
    }

    /**
     * 获取工厂方法生成对象的实例生成器
     * @return 工厂方法生成对象的实例生成器
     */
    public static BeanInstance getFactoryMethodInstance() {
        return BeanInstanceHolder.FACTORY_METHOD_INSTANCE.beanInstance;
    }

    /**
     * 获取JDL动态代理生成对象的实例生成器
     * @return JDL动态代理生成对象的实例生成器
     */
    public static BeanInstance getJdkDynamicProxyInstance() {
        return BeanInstanceHolder.JDK_DYNAMIC_PROXY_INSTANCE.beanInstance;
    }

    /**
     * 枚举生成单例对象
     */
    @AllArgsConstructor
    private enum BeanInstanceHolder {

        /**
         * 构造函数的实例生成器
         */
        CONSTRUCTOR_INSTANCE(new ConstructorInstance()),
        /**
         * 工厂Bean的实例生成器
         */
        FACTORY_BEAN_INSTANCE(new FactoryBeanMethodInstance()),
        /**
         * 工厂方法的实例生成器
         */
        FACTORY_METHOD_INSTANCE(new FactoryMethodInstance()),
        /**
         * JDK动态代理实例化
         */
        JDK_DYNAMIC_PROXY_INSTANCE(new JdkDynamicProxyInstance()),
        ;

        private BeanInstance beanInstance;
    }
}
