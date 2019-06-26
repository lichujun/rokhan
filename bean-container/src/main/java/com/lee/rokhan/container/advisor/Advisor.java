package com.lee.rokhan.container.advisor;

import com.lee.rokhan.container.pointcut.Pointcut;

/**
 * Aop增强器
 * @author lichujun
 * @date 2019/6/18 14:53
 */
public interface Advisor {

    String getAdviceBeanName();

    String getExpression();

    Pointcut getPointcut();
}
