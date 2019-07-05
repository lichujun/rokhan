package com.lee.rokhan.container.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 组件和注入的Bean名称
 * @author lichujun
 * @date 2019/7/5 14:15
 */
@Getter
@AllArgsConstructor
public class ComponentInjection {

    private String beanName;

    private Class<?> componentClass;
}
