package com.lee.rokhan.common.utils.throwable;

/**
 * Predicate异常接口
 * @author lichujun
 * @date 2018/12/9 12:54 AM
 */
@FunctionalInterface
public interface ThrowPredicate<T, E extends Throwable> {

    /**
     * Predicate异常接口方法
     * @param t T对象
     * @return 通过T对象作判断
     */
    boolean test(T t);
}
