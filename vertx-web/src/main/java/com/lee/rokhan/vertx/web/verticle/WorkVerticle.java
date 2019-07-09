package com.lee.rokhan.vertx.web.verticle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.vertx.web.codec.HttpRequest;
import com.lee.rokhan.vertx.web.codec.HttpResponse;
import com.lee.rokhan.vertx.web.context.VertxWebContext;
import com.lee.rokhan.vertx.web.pojo.ControllerInfo;
import com.lee.rokhan.vertx.web.pojo.MethodParamsWithHeaders;
import com.lee.rokhan.vertx.web.pojo.PathInfo;
import com.lee.rokhan.vertx.web.server.VertxWebServer;
import com.lee.rokhan.vertx.web.utils.InvokeControllerUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * work-verticle
 * @author lichujun
 * @date 2019/2/20 7:35 PM
 */
@Slf4j
public class WorkVerticle extends AbstractVerticle {

    private ApplicationContext applicationContext;

    private static AtomicInteger count = new AtomicInteger(0);

    private VertxWebContext vertxWebContext;

    public WorkVerticle(ApplicationContext applicationContext, VertxWebContext vertxWebContext) {
        this.applicationContext = applicationContext;
        this.vertxWebContext = vertxWebContext;
    }

    @Override
    public void start() {
        if (count.getAndIncrement() != 0) {
            return;
        }
        // 处理event loop分发过来的请求
        vertxWebContext.processMessage(this);
    }

    /**
     * 处理event bus分发过来的请求
     *
     * @param path 上下文信息
     * @param controller controller信息
     */
    public void processReq(PathInfo path, ControllerInfo controller) {
        vertx.eventBus().consumer(path.getHttpMethod() + path.getHttpPath(), message -> {
            HttpResponse<String> httpResponse = null;
            try {
                // 获取event bus传递过来的参数
                Optional<HttpRequest> httpRequest = Optional.of(message)
                        .map(Message::body)
                        .map(it -> (HttpRequest) it);

                MultiMap headers = httpRequest.map(HttpRequest::getHeaders)
                        .orElse(null);
                Object res;
                MethodParamsWithHeaders methodParamsWithHeaders = controller.getMethodParamsWithHeaders();
                // 如果无参，直接调用
                if (MapUtils.isEmpty(methodParamsWithHeaders.getMethodParameter())) {
                    res = InvokeControllerUtils.invokeController(controller, headers, applicationContext);
                } else {
                    List<Object> paramList = httpRequest.map(HttpRequest::getParamList)
                            .orElse(null);
                    //log.info("请求路径：【{}】，请求参数：【{}】", path.getHttpPath(),JSON.toJSONString(paramList));
                    res = InvokeControllerUtils.invokeController(controller, paramList, headers, applicationContext);
                }
                httpResponse = HttpResponse.<String>builder()
                        .status(HttpResponseStatus.OK)
                        .response(JSON.toJSONString(res, SerializerFeature.WriteMapNullValue))
                        .build();
            } catch (Throwable e) {
                log.error("err");
            } finally {
                message.reply(httpResponse);
            }
        });
    }
}
