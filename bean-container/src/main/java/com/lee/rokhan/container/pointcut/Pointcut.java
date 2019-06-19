package com.lee.rokhan.container.pointcut;

import java.lang.reflect.Method;

/**
 * 切点
 * @author lichujun
 * @date 2019/6/18 14:32
 */
public interface Pointcut {

    /**
     * 匹配类
     * @param targetClass 目标类对象
     * @return 是否匹配成功
     */
    boolean matchClass(Class<?> targetClass);

    /**
     * 匹配方法
     * @param targetMethod 目标方法
     * @return 是否匹配成功
     */
    boolean matchMethod(Method targetMethod);
}
