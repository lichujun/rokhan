package com.lee.rokhan.container.aware;

/**
 * 让Bean对象获取到Bean名称
 * @author lichujun
 * @date 2019/7/4 11:03
 */
public interface BeanNameAware extends Aware {

    /**
     * 设置Bean名称
     * @param beanName Bean名称
     */
    void setBeanName(String beanName);
}
