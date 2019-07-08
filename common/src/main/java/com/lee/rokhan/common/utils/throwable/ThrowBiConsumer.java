package com.lee.rokhan.common.utils.throwable;

/**
 * 两个参数的Consumer
 * @author lichujun
 * @date 2019/7/8 11:08
 */
@FunctionalInterface
public interface ThrowBiConsumer<T, U, E extends Throwable> {

    /**
     * Consumer异常接口方法
     * @param t T对象
     * @param u U对象
     * @throws E 异常
     */
    void accept(T t, U u) throws E;
}
