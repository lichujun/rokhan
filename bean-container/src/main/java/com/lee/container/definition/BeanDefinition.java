package com.lee.container.definition;

import org.apache.commons.lang3.StringUtils;

/**
 * Bean注册信息
 *
 * @author lichujun
 * @date 2019/6/17 10:58
 */

public interface BeanDefinition {

    String SCOPE_SINGLETON = "singleton";
    String SCOPE_PROTOTYPE = "prototype";

    Class<?> getBeanClass();

    String getScope();

    boolean isSingleton();

    boolean isPrototype();

    String getFactoryBeanName();

    String getFactoryMethodName();

    String getInitMethodName();

    String getDestroyMethodName();

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
