package com.lee.rokhan.container.aware;

import com.lee.rokhan.container.context.ApplicationContext;

/**
 * 让Bean对象获取到应用上下文
 * @author lichujun
 * @date 2019/7/4 11:05
 */
public interface ApplicationContextAware extends Aware {

    /**
     * 设置应用上下文
     * @param applicationContext  应用上下文
     */
    void setApplicationContext(ApplicationContext applicationContext) throws Throwable;
}
