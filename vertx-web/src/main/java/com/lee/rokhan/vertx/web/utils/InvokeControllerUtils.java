package com.lee.rokhan.vertx.web.utils;

import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.vertx.web.pojo.ControllerInfo;
import com.lee.rokhan.vertx.web.pojo.MethodParam;
import com.lee.rokhan.vertx.web.pojo.MethodParamsWithHeaders;
import io.vertx.core.MultiMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author lichujun
 * @date 2019/2/8 3:03 PM
 */
@Slf4j
public class InvokeControllerUtils {

    /**
     * 调用controller的方法
     * @param controllerInfo controller的信息
     * @return 返回报文
     */
    public static Object invokeController(ControllerInfo controllerInfo, MultiMap headers, ApplicationContext applicationContext) throws Throwable {
        Object controller = applicationContext.getBean(controllerInfo.getBeanName());
        Method method = controllerInfo.getInvokeMethod();
        method.setAccessible(true);
        try {
            MethodParamsWithHeaders methodParamsWithHeaders = controllerInfo.getMethodParamsWithHeaders();
            if (methodParamsWithHeaders.getHeadersPosition() != null) {
                return method.invoke(controller, headers);
            } else {
                return method.invoke(controller);
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    /**
     * 调用controller的方法
     * @param controllerInfo controller的信息
     * @param paramList 参数集合
     * @return 返回报文
     */
    public static Object invokeController(ControllerInfo controllerInfo, List<Object> paramList, MultiMap headers, ApplicationContext applicationContext) throws Throwable {
        Object controller = applicationContext.getBean(controllerInfo.getBeanName());
        Method method = controllerInfo.getInvokeMethod();
        method.setAccessible(true);
        MethodParamsWithHeaders methodParamsWithHeaders = controllerInfo.getMethodParamsWithHeaders();
        Map<String, MethodParam> paramClassMap = methodParamsWithHeaders.getMethodParameter();
        if (MapUtils.isEmpty(paramClassMap)) {
            return invokeController(controllerInfo, headers, applicationContext);
        }
        try {
            Integer position = methodParamsWithHeaders.getHeadersPosition();
            if (position != null) {
                paramList.add(position, headers);
            }
            return method.invoke(controller, paramList.toArray());
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
