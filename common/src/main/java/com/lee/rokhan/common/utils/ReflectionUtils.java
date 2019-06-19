package com.lee.rokhan.common.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 反射的工具类
 *
 * @author lichujun
 * @date 2019/6/18 17:22
 */
@Slf4j
public abstract class ReflectionUtils {

    /**
     * 通过类对象获取子类和所有父类的方法Method
     *
     * @param object 子类对象
     * @return 子类和所有父类的方法Method
     */
    public static Set<Method> getDeclaredMethods(Object object) {
        if (object == null) {
            return Collections.emptySortedSet();
        }
        return getDeclaredMethods(object.getClass());
    }

    /**
     * 通过类对象获取子类和所有父类的方法Method
     *
     * @param clazz 子类的类对象
     * @return 子类和所有父类的方法Method
     */
    public static Set<Method> getDeclaredMethods(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptySortedSet();
        }
        Set<Method> methodSet = new LinkedHashSet<>();
        Set<MethodSignature> methodSignatureSet = new HashSet<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                MethodSignature methodSignature = new MethodSignature(method.getName(), method.getParameterTypes());
                if (methodSignatureSet.add(methodSignature)) {
                    methodSet.add(method);
                }
            }
        }
        return methodSet;
    }

    /**
     * 方法签名
     */
    @AllArgsConstructor
    private static class MethodSignature {
        private final String methodName;
        private final Class<?>[] methodParameters;

        @Override
        public int hashCode() {
            int result = Objects.hash(methodName);
            return 31 * result + 37 * Optional.ofNullable(methodParameters)
                    .map(it -> it.length)
                    .orElse(0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodSignature that = (MethodSignature) o;
            return methodName.equals(that.methodName) &&
                    Arrays.equals(methodParameters, that.methodParameters);
        }
    }

    /**
     * 通过类对象获取子类和所有父类的成员变量Field
     *
     * @param object 子类对象
     * @return 子类和所有父类的成员变量Field
     */
    public static Set<Field> getDeclaredFields(Object object) {
        if (object == null) {
            return Collections.emptySortedSet();
        }
        return getDeclaredFields(object.getClass());
    }

    /**
     * 通过类对象获取子类和所有父类的成员变量Field
     *
     * @param clazz 子类的类对象
     * @return 子类和所有父类的成员变量Field
     */
    public static Set<Field> getDeclaredFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptySortedSet();
        }
        Set<Field> fieldSet = new LinkedHashSet<>();
        Set<String> fieldNameSet = new HashSet<>();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (fieldNameSet.add(field.getName())) {
                    fieldSet.add(field);
                }
            }
        }
        return fieldSet;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param clazz          子类的类对象
     * @param methodName     父类中的方法名
     * @param parameterTypes 父类中的方法参数类型
     * @return 父类中的方法对象
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method;
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                return method;
            } catch (NoSuchMethodException e) {
                // 这里甚么都不需要做
            }
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     *
     * @param object         子类的对象
     * @param methodName     父类中的方法名
     * @param parameterTypes 父类中的方法参数类型
     * @return 父类中的方法对象
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>... parameterTypes) {
        Objects.requireNonNull(object, "获取方法的对象不能为空");
        return getDeclaredMethod(object.getClass(), methodName, parameterTypes);
    }

    /**
     * 直接调用对象方法, 而忽略修饰符(private, protected, default)
     *
     * @param object         子类对象
     * @param methodName     父类中的方法名
     * @param parameterTypes 父类中的方法参数类型
     * @param parameters     父类中的方法参数
     * @return 父类中方法的执行结果
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes,
                                      Object[] parameters) {
        // 根据 对象、方法名和对应的方法参数 通过反射 调用上面的方法获取 Method 对象
        Method method = getDeclaredMethod(object, methodName, parameterTypes);
        // 抑制Java对方法进行检查,主要是针对私有方法而言
        method.setAccessible(true);
        try {
            // 调用object 的 method 所代表的方法，其方法的参数是 parameters
            return method.invoke(object, parameters);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            log.error("方法调用发生异常", e);
        }
        return null;
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object    子类对象
     * @param fieldName 父类中的属性名
     * @return 父类中的属性对象
     */
    public static Field getDeclaredField(Object object, String fieldName) {
        Field field;
        Class<?> clazz = object.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                field = clazz.getDeclaredField(fieldName);
                return field;
            } catch (Exception e) {
                // 这里甚么都不需要做
            }
        }
        return null;
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     *
     * @param object    子类对象
     * @param fieldName 父类中的属性名
     * @param value     将要设置的值
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        // 根据 对象和属性名通过反射 调用上面的方法获取 Field对象
        Field field = getDeclaredField(object, fieldName);
        if (field == null) {
            return;
        }
        // 抑制Java对其的检查
        field.setAccessible(true);
        try {
            //将 object 中 field 所代表的值 设置为 value
            field.set(object, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            log.error("注入成员变量[{}]发生异常", fieldName, e);
        }
    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     *
     * @param object    子类对象
     * @param fieldName 父类中的属性名
     * @return 父类中的属性值
     */
    public static Object getFieldValue(Object object, String fieldName) {
        // 根据 对象和属性名通过反射 调用上面的方法获取 Field对象
        Field field = getDeclaredField(object, fieldName);
        if (field == null) {
            return null;
        }
        // 抑制Java对其的检查
        field.setAccessible(true);
        try {
            // 获取 object 中 field 所代表的属性值
            return field.get(object);
        } catch (Exception e) {
            log.error("获取成员变量的值发生异常", e);
        }
        return null;
    }

}