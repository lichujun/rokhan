package com.lee.container.definition;

import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Bean注册信息
 *
 * @author lichujun
 * @date 2019/6/17 10:58
 */

public interface BeanDefinition {

    /**
     * 单例
     */
    String SCOPE_SINGLETON = "singleton";

    /**
     * 原型
     */
    String SCOPE_PROTOTYPE = "prototype";

    Class<?> getBeanClass();

    String getScope();

    boolean isSingleton();

    boolean isPrototype();

    String getFactoryBeanName();

    String getFactoryMethodName();

    String getInitMethodName();

    String getDestroyMethodName();

    List<Object> getConstructorArgumentValues();

    //以下4个方法仅供BeanFactory使用，对于prototype,缓存构造方法或工厂方法
    Constructor<?> getConstructor();

    void setConstructor(Constructor<?> constructor);

    Method getFactoryMethod();

    void setFactoryMethod(Method method);


    default boolean validate() {
        // class没指定,工厂bean或工厂method不指定皆为不合法情况
        if (getBeanClass() == null) {
            if (StringUtils.isBlank(getFactoryBeanName()) || StringUtils.isBlank(getFactoryMethodName())) {
                return false;
            }
        }

        // class和工厂bean同时存在不合法
        return getBeanClass() == null || StringUtils.isBlank(getFactoryBeanName());
    }
}
