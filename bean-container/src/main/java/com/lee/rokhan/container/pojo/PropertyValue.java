package com.lee.rokhan.container.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 依赖
 * @author lichujun
 * @date 2019/6/18 10:54
 */
@Getter
@AllArgsConstructor
public class PropertyValue {
    /**
     * Field名称
     */
    private final String name;

    /**
     * Field对象
     */
    private final Object value;

    private String beanName;
}
