package com.lee.rokhan.vertx.web.context;

import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.annotation.Controller;
import com.lee.rokhan.container.aware.ApplicationContextAware;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.vertx.web.annotation.Header;
import com.lee.rokhan.vertx.web.annotation.RequestMapping;
import com.lee.rokhan.vertx.web.annotation.RequestParam;
import com.lee.rokhan.vertx.web.pojo.*;
import com.lee.rokhan.vertx.web.verticle.EventLoopVerticle;
import com.lee.rokhan.vertx.web.verticle.WorkVerticle;
import io.vertx.core.MultiMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 初始化web
 * @author lichujun
 * @date 2019/7/9 12:35
 */
@Component
public class VertxWebContext implements ApplicationContextAware {

    private final Map<PathInfo, ControllerInfo> pathControllerMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws Throwable {
        // 加载所有的Bean
        applicationContext.processAllBeanDefinition((beanName, beanDefinition) ->
                applicationContext.getBean(beanName));
        // 加载Controller
        applicationContext.processComponentProperty(componentProperty -> {
            Class<?> clazz = componentProperty.getClazz();
            String beanName = componentProperty.getInjectionProperty().getBeanName();
            processPathController(clazz, beanName);
        }, Controller.class);
    }



    /**
     * 将单个Controller的Class对象的上下文信息和Controller关系进行绑定
     * @param tClass Controller的Class对象
     */
    private void processPathController(Class<?> tClass, String beanName) {
        if (tClass == null) {
            return;
        }
        // 获取类@RequestMapping注入的值，获取Controller类的上下文
        String basePath = Optional.ofNullable(tClass.getDeclaredAnnotation(
                RequestMapping.class))
                .map(RequestMapping::value)
                .map(this::completeSeparator)
                .orElse("");
        List<Method> methodList = Optional.ofNullable(tClass.getDeclaredMethods())
                // 获取方法上有@RequestMapping注解的方法
                .map(methods -> Stream.of(methods)
                        .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                        .collect(Collectors.toList())
                )
                .orElse(null);
        if (CollectionUtils.isEmpty(methodList)) {
            return;
        }
        // 扫描所有方法的RequestMapping注解
        for (Method method : methodList) {
            RequestMapping reqMapping = Optional.ofNullable(method)
                    .filter(it -> !Void.TYPE.equals(it.getReturnType()))
                    .map(it -> it.getDeclaredAnnotation(RequestMapping.class))
                    .orElse(null);
            if (reqMapping == null) {
                continue;
            }
            // 获取方法上@RequestMapping注入的值，拼接上下文
            String httpPath = basePath + Optional.of(reqMapping)
                    .map(RequestMapping::value)
                    .map(this::completeSeparator)
                    .orElse("");
            // 请求方法
            RequestMethod reqMethod = Optional.of(reqMapping)
                    .map(RequestMapping::method)
                    .orElse(RequestMethod.GET);
            // 获取方法的参数
            MethodParamsWithHeaders methodParamsWithHeaders = new MethodParamsWithHeaders();
            Map<String, MethodParam> paramMap = null;
            Parameter[] parameters = method.getParameters();
            if (ArrayUtils.isNotEmpty(parameters)) {
                paramMap = new LinkedHashMap<>();
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    if (parameter.isAnnotationPresent(Header.class)) {
                        if (parameter.getType().equals(MultiMap.class)) {
                            if (methodParamsWithHeaders.getHeadersPosition() != null) {
                                throw new RuntimeException(method + "方法参数不能存在多个Header参数");
                            } else {
                                methodParamsWithHeaders.setHeadersPosition(i);
                            }
                        } else {
                            throw new RuntimeException(method + "参数headers只能使用MultiMap类型");
                        }
                    } else {
                        String name = Optional.of(parameter)
                                // 获取@RequestParam注入的值，参数名
                                .map(it -> it.getDeclaredAnnotation(RequestParam.class))
                                .map(RequestParam::value)
                                .filter(StringUtils::isNotBlank)
                                .orElse(StringUtils.uncapitalize(parameter.getType()
                                        .getSimpleName()));
                        Type type = parameter.getParameterizedType();
                        Class<?> paramClass = parameter.getType();
                        MethodParam methodParam = new MethodParam(paramClass, type);
                        Optional.ofNullable(paramMap.put(name, methodParam))
                                .ifPresent(it -> {
                                    throw new RuntimeException(String.format(
                                            "参数名称不能相同，发生错误的方法：%s.%s",
                                            tClass.getName(), method.getName()));
                                });
                    }

                }
            }
            methodParamsWithHeaders.setMethodParameter(paramMap);
            if (reqMethod == RequestMethod.ALL) {
                for (RequestMethod reqMethodEnum : RequestMethod.values()) {
                    if (reqMethodEnum == RequestMethod.ALL) {
                        continue;
                    }
                    putControllerInfo(httpPath, reqMethodEnum, tClass, method, methodParamsWithHeaders, beanName);
                }
            } else {
                putControllerInfo(httpPath, reqMethod, tClass, method, methodParamsWithHeaders, beanName);
            }
        }
    }

    /**
     * 字符串前面补充/
     * @param origin 传入的字符串
     * @return 处理后的字符串
     */
    private String completeSeparator(String origin) {
        return Optional.ofNullable(origin)
                .map(it -> it.startsWith("/") ? it : "/" + it)
                .orElse("");
    }

    /**
     * 将ControllerInfo信息存放在容器中
     */
    private void putControllerInfo(String httpPath, RequestMethod reqMethod,
                                   Class<?> tClass, Method method,
                                   MethodParamsWithHeaders methodParamsWithHeaders,
                                   String beanName) {
        PathInfo pathInfo = new PathInfo(httpPath, reqMethod.toString());
        ControllerInfo controllerInfo = new ControllerInfo(tClass, method, methodParamsWithHeaders, beanName);
        if (pathControllerMap.put(pathInfo, controllerInfo) != null) {
            throw new RuntimeException(String.format(
                    "存在相同的上下文和http请求方法，controller层的方法在：%s.%s",
                    tClass.getName(), method.getName()));
        }
    }

    /**
     * 接收vertx来自event loop分发过来的请求
     */
    public void processMessage(WorkVerticle work) {
        pathControllerMap.forEach(work::processReq);
    }

    /**
     * event loop路由请求
     */
    public void routeMessage(EventLoopVerticle loop) {
        pathControllerMap.forEach(loop::routeReq);
        loop.routeNotFound();
    }
}
