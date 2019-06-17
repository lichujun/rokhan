package com.lee.container.definition.impl;

import com.lee.container.definition.BeanDefinition;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Bean容器注册信息
 *
 * @author lichujun
 * @date 2019/6/17 11:31
 */
@Data
public class IocBeanDefinition implements BeanDefinition {

    /**
     * bean的类对象
     */
    private Class<?> beanClass;

    /**
     * scope类型
     */
    private String scope = BeanDefinition.SCOPE_SINGLETON;

    /**
     * 工厂Bean名称
     */
    private String factoryBeanName;

    /**
     * 工厂方法名称
     */
    private String factoryMethodName;

    /**
     * 初始化方法
     */
    private String initMethodName;

    /**
     * 销毁方法
     */
    private String destroyMethodName;

    /**
     * 构造函数的参数对象列表
     */
    private List<Object> constructorArgumentValues;

    /**
     * 构造函数
     * 注：用于缓存，用于生成prototype类型的对象
     */
    private Constructor<?> constructor;

    /**
     * 工厂方法
     * 注：用于缓存，用于生成prototype类型的对象
     */
    private Method factoryMethod;

    public void setScope(String scope) {
        if (StringUtils.isNotBlank(scope)) {
            this.scope = scope;
        }
    }

    @Override
    public boolean isSingleton() {
        return BeanDefinition.SCOPE_SINGLETON.equals(this.scope);
    }

    @Override
    public boolean isPrototype() {
        return BeanDefinition.SCOPE_PROTOTYPE.equals(this.scope);
    }

}
