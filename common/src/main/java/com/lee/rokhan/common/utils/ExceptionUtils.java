package com.lee.rokhan.common.utils;

import com.lee.rokhan.common.utils.throwable.*;

import java.util.function.*;

/**
 * 函数式异常处理
 * @author lichujun
 * @date 2018/12/9 12:27 AM
 */
public class ExceptionUtils {

    /** Consumer抛出异常 */
    public static <T, E extends Throwable> Consumer<T> handleConsumer(ThrowConsumer<T, E> consumer) {
        return it -> {
            try {
                consumer.accept(it);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** BiConsumer抛出异常 */
    public static <T, U, E extends Throwable> BiConsumer<T, U> handleConsumer(ThrowBiConsumer<T, U, E> biConsumer) {
        return (t, u) -> {
            try {
                biConsumer.accept(t, u);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** Supplier抛出异常 */
    public static <T, E extends Throwable> Supplier<T> handleSupplier(ThrowSupplier<T, E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** Function抛出异常 */
    public static <T, K, E extends Throwable> Function<T, K> handleFunction(
            ThrowFunction<T, K, E> function) {
        return t -> {
          try {
              return function.apply(t);
          } catch (Throwable e) {
              throw new RuntimeException(e);
          }
        };
    }

    /** Predicate抛出异常 */
    public static <T, E extends Throwable> Predicate<T> handlePredicate(ThrowPredicate<T, E> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
