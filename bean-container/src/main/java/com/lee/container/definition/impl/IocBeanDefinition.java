package com.lee.container.definition.impl;

import com.lee.container.definition.BeanDefinition;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Bean容器注册信息
 * @author lichujun
 * @date 2019/6/17 11:31
 */
@Data
public class IocBeanDefinition implements BeanDefinition {

    private Class<?> beanClass;

    private String scope = BeanDefinition.SCOPE_SINGLETON;

    private  String factoryBeanName;

    private String factoryMethodName;

    private String initMethodName;

    private String destroyMethodName;

    public void setScope(String scope) {
        if (StringUtils.isNotBlank(scope)){
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
