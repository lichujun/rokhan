package com.lee.container.pojo;

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
     * Bean名称
     */
    private final String name;

    /**
     * Bean对象
     */
    private final Object value;
}
