package com.lee.rokhan.common.utils;

import com.lee.rokhan.common.utils.throwable.ThrowConsumer;
import com.lee.rokhan.common.utils.throwable.ThrowFunction;
import com.lee.rokhan.common.utils.throwable.ThrowPredicate;
import com.lee.rokhan.common.utils.throwable.ThrowSupplier;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 函数式异常处理
 * @author lichujun
 * @date 2018/12/9 12:27 AM
 */
public class ExceptionUtils {

    /** Consumer抛出异常 */
    public static <T, E extends Exception> Consumer<T> handleConsumer(ThrowConsumer<T, E> consumer) {
        return it -> {
            try {
                consumer.accept(it);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** Supplier抛出异常 */
    public static <T, E extends Exception> Supplier<T> handleSupplier(ThrowSupplier<T, E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** Function抛出异常 */
    public static <T, K, E extends Exception> Function<T, K> handleFunction(
            ThrowFunction<T, K, E> function) {
        return t -> {
          try {
              return function.apply(t);
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
        };
    }

    /** Predicate抛出异常 */
    public static <T, E extends Exception> Predicate<T> handlePredicate(ThrowPredicate<T, E> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}
