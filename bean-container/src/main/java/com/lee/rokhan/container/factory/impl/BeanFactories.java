package com.lee.rokhan.container.factory.impl;

import com.lee.rokhan.container.factory.BeanFactory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 生成bean工厂的静态工厂
 * @author lichujun
 * @date 2019/6/17 15:06
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanFactories {

    /**
     * 获取Ioc的bean工厂的实例
     * @return Ioc的bean工厂
     */
    public static BeanFactory getIocBeanFactory() {
        return BeanFactoryHolder.IOC_BEAM_FACTORY.beanFactory;
    }

    /**
     * 枚举生成单例对象
     */
    @AllArgsConstructor
    private enum BeanFactoryHolder {
        IOC_BEAM_FACTORY(new IocBeanFactory()),
        ;

        private BeanFactory beanFactory;
    }
}
