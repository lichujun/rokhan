package com.lee.rokhan.vertx.web.verticle;

import com.alibaba.fastjson.JSON;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.vertx.web.codec.HttpRequest;
import com.lee.rokhan.vertx.web.codec.HttpResponse;
import com.lee.rokhan.vertx.web.context.VertxWebContext;
import com.lee.rokhan.vertx.web.pojo.*;
import com.lee.rokhan.vertx.web.utils.AsyncResultUtils;
import com.lee.rokhan.vertx.web.utils.ParseParamUtils;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.*;

/**
 * event loop
 * @author lichujun
 * @date 2019/2/20 7:35 PM
 */
public class EventLoopVerticle extends AbstractVerticle {

    private Router router;

    private ApplicationContext applicationContext;

    private VertxWebContext vertxWebContext;

    public EventLoopVerticle(ApplicationContext applicationContext, VertxWebContext vertxWebContext) {
        this.applicationContext = applicationContext;
        this.router = Router.router(vertx);
        this.vertxWebContext = vertxWebContext;
    }


    @Override
    public void start() {
        // 路由请求
        vertxWebContext.routeMessage(this);

        // 监听端口
        vertx.createHttpServer(new HttpServerOptions()
                .setMaxWebsocketFrameSize(1024 * 1024 * 10)
                .setCompressionSupported(true)
                .setTcpKeepAlive(true)
                .setReuseAddress(true))
                .requestHandler(router)
                .listen(9000);
    }

    /**
     * 路由请求
     */
    public void routeReq(PathInfo path, ControllerInfo controller) {
        if (HttpMethod.GET.name().equals(path.getHttpMethod())) {
            routeGetReq(path, controller);
        } else if (HttpMethod.POST.name().equals(path.getHttpMethod())) {
            routePostReq(path, controller);
        }
    }

    /**
     * 404请求
     */
    public void routeNotFound() {
        router.route()
                // 跨域
                .handler(CorsHandler.create("*").allowedMethod(io.vertx.core.http.HttpMethod.POST))
                .handler(CorsHandler.create("*").allowedMethod(io.vertx.core.http.HttpMethod.GET))
                .handler(rc -> rc.response()
                .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
                .end());
    }

    /**
     * 路由GET请求
     */
    private void routeGetReq(PathInfo pathInfo, ControllerInfo controllerInfo) {
        router.get(pathInfo.getHttpPath())
                // 跨域
                .handler(CorsHandler.create("*").allowedMethod(io.vertx.core.http.HttpMethod.GET))
                .handler(rc ->
                    processRoute(pathInfo, controllerInfo, rc, RequestMethod.GET)
                );
    }

    /**
     * 路由POST请求
     */
    private void routePostReq(PathInfo pathInfo, ControllerInfo controllerInfo) {
        router.post(pathInfo.getHttpPath())
                // 跨域
                .handler(CorsHandler.create("*").allowedMethod(io.vertx.core.http.HttpMethod.POST))
                // 为获取post请求的body，必须要加
                .handler(BodyHandler.create())
                .handler(rc ->
                        processRoute(pathInfo, controllerInfo, rc, RequestMethod.POST)
                );
    }

    /**
     * EventBus发送消息
     */
    private void sendMessage(EventBus eb, String path, Object msg, RoutingContext rc) {
        eb.send(path, msg, res -> {
            HttpResponse<String> httpResponse = AsyncResultUtils.transResponse(res);
            if (HttpResponseStatus.OK.equals(httpResponse.getStatus())) {
                rc.response()
                        .putHeader("Content-type", "text/plain;charset=UTF-8")
                        .end(httpResponse.getResponse());
            } else {
                HttpResponseStatus status = httpResponse.getStatus();
                if (status == null) {
                    status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                }
                rc.response()
                        .setStatusCode(status.code())
                        .end();
            }
        });
    }

    /**
     * 进行请求解析参数和分发请求
     */
    private void processRoute(PathInfo pathInfo, ControllerInfo controllerInfo,
                       RoutingContext rc, RequestMethod requestMethod) {
        EventBus eb = vertx.eventBus();
        // event bus传递消息的路径
        String path = pathInfo.getHttpMethod() + pathInfo.getHttpPath();
        MethodParamsWithHeaders methodParamsWithHeaders = controllerInfo.getMethodParamsWithHeaders();
        Map<String, MethodParam> paramMap = methodParamsWithHeaders.getMethodParameter();
        // 获取headers
        MultiMap headers = rc.request().headers();
        // 入参为空，则无需解析请求参数
        if (MapUtils.isEmpty(paramMap)) {
            sendMessage(eb, path, new HttpRequest(null, headers), rc);
            return;
        }
        final Map<String, String> params = new HashMap<>();
        List<Object> paramList = null;
        // 获取请求参数
        if (RequestMethod.POST.equals(requestMethod)) {
            for (String param : paramMap.keySet()) {
                Optional.ofNullable(rc.request().getFormAttribute(param))
                        .ifPresent(it -> params.put(param, it));
            }
            // 获取body
            if (MapUtils.isEmpty(params) && paramMap.size() == 1) {
                String body = rc.getBodyAsString();
                MethodParam methodParam = paramMap.values().stream()
                        .findFirst()
                        .orElse(null);
                if (methodParam != null) {
                    paramList = Optional.ofNullable(body)
                            .filter(StringUtils::isNotBlank)
                            .map(it -> JSON.parseObject(it, methodParam.getType()))
                            .map(it -> new ArrayList<Object>() {{
                                add(it);
                            }})
                            .orElse(null);
                }
            } else {
                paramList = ParseParamUtils.parse(methodParamsWithHeaders.getMethodParameter(), params);
            }
        } else if (RequestMethod.GET.equals(requestMethod)){
            for (String param : paramMap.keySet()) {
                Optional.ofNullable(rc.request().getParam(param))
                        .ifPresent(it -> params.put(param, it));
            }
            if (MapUtils.isNotEmpty(params)) {
                paramList = ParseParamUtils.parse(methodParamsWithHeaders.getMethodParameter(), params);
            }
        }
        if (CollectionUtils.isEmpty(paramList)) {
            rc.response()
                    .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end();
            return;
        }
        HttpRequest httpRequest = new HttpRequest(paramList, headers);
        sendMessage(eb, path, httpRequest, rc);
    }

}
