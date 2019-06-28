package com.lee.rokhan.container.intercept;

/**
 * 连接点
 * @author lichujun
 * @date 2019/6/28 11:17
 */
public interface JoinPoint {

    /**
     * 执行原有对象的方法
     * @return 原有对象的返回值
     * @throws Throwable 异常
     */
    Object proceed() throws Throwable;
}
