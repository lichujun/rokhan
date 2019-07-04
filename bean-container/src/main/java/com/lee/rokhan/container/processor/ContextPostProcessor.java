package com.lee.rokhan.container.processor;

import com.lee.rokhan.container.context.ApplicationContext;

/**
 * 应用上下文初始化前或后
 * @author lichujun
 * @date 2019/7/4 16:18
 */
public interface ContextPostProcessor {

    void postProcessBeforeInitialization(ApplicationContext context) throws Throwable;

    void postProcessAfterInitialization(ApplicationContext context) throws Throwable;
}
