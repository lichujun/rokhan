package com.lee.rokhan.container.definition.impl;

import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.pojo.BeanReference;
import com.lee.rokhan.container.pojo.PropertyValue;
import com.lee.rokhan.container.definition.BeanDefinition;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Ioc Bean容器注册信息
 *
 * @author lichujun
 * @date 2019/6/17 11:31
 */
@Data
public class IocBeanDefinition implements BeanDefinition {

    /**
     * Bean的类对象
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
    private List<Object> argumentValues;

    /**
     * 构造函数
     * 注：用于缓存，用于生成prototype类型的对象
     */
    private Constructor<?> constructor;

    /**
     * 依赖
     */
    private List<PropertyValue> propertyValues;

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
    public Object[] getArgumentRealValues(BeanFactory beanFactory) throws Throwable {
        if (CollectionUtils.isEmpty(argumentValues)) {
            return null;
        }
        Object[] values = new Object[argumentValues.size()];
        int i = 0;
        //values数组的元素
        Object value;
        for (Object realValue : argumentValues) {
            if (realValue == null) {
                value = null;
            } else if (realValue instanceof BeanReference) {
                value = beanFactory.getBean(((BeanReference) realValue).getBeanName());
            } else {
                value = realValue;
            }
            values[i++] = value;
        }
        return values;
    }

    /**
     * 新增依赖关系
     * @param propertyValue 依赖
     */
    @Override
    public void addPropertyValue(PropertyValue propertyValue) {
        if (propertyValues == null) {
            propertyValues = new ArrayList<>();
        }
        propertyValues.add(propertyValue);
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
