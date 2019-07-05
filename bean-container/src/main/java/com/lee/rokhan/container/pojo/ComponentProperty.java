package com.lee.rokhan.container.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 组件属性
 * @author lichujun
 * @date 2019/7/5 14:05
 */
@Getter
@AllArgsConstructor
public class ComponentProperty {

    /**
     * 组件标注的类对象
     */
    private final Class<?> clazz;

    /**
     * 注入的组件和属性
     */
    private InjectionProperty injectionProperty;
}
