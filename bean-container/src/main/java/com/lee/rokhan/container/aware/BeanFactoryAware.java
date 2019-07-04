package com.lee.rokhan.container.aware;

import com.lee.rokhan.container.factory.BeanFactory;

/**
 * 让Bean对象获取到Bean工厂
 * @author lichujun
 * @date 2019/7/4 11:04
 */
public interface BeanFactoryAware extends Aware {

    /**
     * 设置Bean工厂
     * @param beanFactory Bean工厂
     */
    void setBeanFactory(BeanFactory beanFactory) throws Throwable;
}
