package com.lee.container.instance.impl;

import com.lee.container.definition.BeanDefinition;
import com.lee.container.instance.BeanInstance;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lichujun
 * @date 2019/6/17 15:01
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ConstructorInstance implements BeanInstance {

    /**
     * 通过构造函数实例化Bean对象
     *
     * @param beanDefinition Bean注册信息
     * @return Bean对象
     * @throws Exception 异常
     */
    @Override
    public Object instance(BeanDefinition beanDefinition) throws Exception {
        try {
            return beanDefinition.getBeanClass().newInstance();
        } catch (SecurityException e) {
            log.error("创建bean的实例异常,beanDefinition" + beanDefinition, e);
            throw e;
        }
    }
}
