package com.lee.rokhan.common.utils.throwable;

/**
 * Consumer异常接口
 * @author lichujun
 * @date 2018/12/9 12:49 AM
 */
@FunctionalInterface
public interface ThrowConsumer<T, E extends Throwable> {

    /**
     * Consumer异常接口方法
     * @param t T对象
     * @throws E 异常
     */
    void accept(T t) throws E;
}
