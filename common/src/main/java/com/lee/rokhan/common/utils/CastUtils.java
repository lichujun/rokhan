package com.lee.rokhan.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Type;

/**
 * 反序列化对象
 * @author lichujun
 * @date 2019/3/15 11:43 PM
 */
@Slf4j
public class CastUtils {

    /**
     * String类型转换成对应类型
     *
     * @param paramClass  转换的类
     * @param value 值
     * @return 转换后的Object
     */
    public static Object convert(String value, Type type, Class<?> paramClass) {
        if (isPrimitive(paramClass)) {
            if (StringUtils.isBlank(value)) {
                return primitiveNull(paramClass);
            }

            if (paramClass.equals(int.class) || paramClass.equals(Integer.class)) {
                return Integer.parseInt(value);
            } else if (paramClass.equals(String.class)) {
                return value;
            } else if (paramClass.equals(Double.class) || paramClass.equals(double.class)) {
                return Double.parseDouble(value);
            } else if (paramClass.equals(Float.class) || paramClass.equals(float.class)) {
                return Float.parseFloat(value);
            } else if (paramClass.equals(Long.class) || paramClass.equals(long.class)) {
                return Long.parseLong(value);
            } else if (paramClass.equals(Boolean.class) || paramClass.equals(boolean.class)) {
                return Boolean.parseBoolean(value);
            } else if (paramClass.equals(Short.class) || paramClass.equals(short.class)) {
                return Short.parseShort(value);
            } else if (paramClass.equals(Byte.class) || paramClass.equals(byte.class)) {
                return Byte.parseByte(value);
            }
            return value;
        } else {
            try {
                return JSON.parseObject(value, type);
            } catch (Throwable e) {
                log.error("json字符串反序列化成对象失败", e);
                return null;
            }
        }
    }

    /**
     * 返回基本数据类型的空值
     *
     * @param type 类
     * @return 对应的空值
     */
    private static Object primitiveNull(Class<?> type) {
        if (type.equals(int.class) || type.equals(double.class) ||
                type.equals(short.class) || type.equals(long.class) ||
                type.equals(byte.class) || type.equals(float.class)) {
            return 0;
        }
        if (type.equals(boolean.class)) {
            return false;
        }
        return null;
    }


    /**
     * 判定是否基本数据类型(包括包装类)
     *
     * @param type 类
     * @return 是否为基本数据类型
     */
    private static boolean isPrimitive(Class<?> type) {
        return type == boolean.class
                || type == Boolean.class
                || type == double.class
                || type == Double.class
                || type == float.class
                || type == Float.class
                || type == short.class
                || type == Short.class
                || type == int.class
                || type == Integer.class
                || type == long.class
                || type == Long.class
                || type == String.class
                || type == byte.class
                || type == Byte.class
                || type == char.class
                || type == Character.class;
    }
}
