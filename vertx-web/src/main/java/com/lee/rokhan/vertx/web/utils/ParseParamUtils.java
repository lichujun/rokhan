package com.lee.rokhan.vertx.web.utils;

import com.alibaba.fastjson.JSON;
import com.lee.rokhan.vertx.web.pojo.MethodParam;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author lichujun
 * @date 2019/3/15 19:04
 */
public class ParseParamUtils {

    public static List<Object> parse(Map<String, MethodParam> paramClassMap,
                                      Map<String, String> paramMap) {
        List<Object> paramList = new ArrayList<>();
        try {
            for (Map.Entry<String, MethodParam> entry : paramClassMap.entrySet()) {
                Object param = getParamObject(paramMap, entry.getKey(), entry.getValue());
                if (param == null) {
                    return null;
                }
                paramList.add(param);
            }
        } catch (Throwable e) {
            return null;
        }
        return paramList;
    }

    /**
     * 获取参数的对象
     * @param paramMap 参数Map
     * @param paramName 参数名称
     * @param methodParam 参数类型
     * @return 参数对象
     */
    private static Object getParamObject(Map<String, String> paramMap,
                                         String paramName,
                                         MethodParam methodParam) {
        return Optional.ofNullable(paramName)
                .map(paramMap::get)
                .filter(StringUtils::isNotBlank)
                .map(it -> JSON.parseObject(it, methodParam.getType()))
                .orElse(null);
    }
}
