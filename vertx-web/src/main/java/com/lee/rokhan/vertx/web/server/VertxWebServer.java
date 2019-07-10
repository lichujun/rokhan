package com.lee.rokhan.vertx.web.server;

import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.context.impl.AnnotationApplicationContext;
import com.lee.rokhan.vertx.web.codec.HttpRequest;
import com.lee.rokhan.vertx.web.codec.HttpRequestCodec;
import com.lee.rokhan.vertx.web.codec.HttpResponse;
import com.lee.rokhan.vertx.web.codec.HttpResponseCodec;
import com.lee.rokhan.vertx.web.context.VertxWebContext;
import com.lee.rokhan.vertx.web.verticle.EventLoopVerticle;
import com.lee.rokhan.vertx.web.verticle.WorkVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import java.util.concurrent.TimeUnit;

/**
 * @author lichujun
 * @date 2019/7/9 10:56
 */
public class VertxWebServer implements WebServer {

    @Override
    public void startServer() throws Throwable {
        Vertx vertx = Vertx.vertx();
        // 设置event bus编解码，用于work-verticle解析event-loop通过event bus传递的数据
        vertx.eventBus().registerDefaultCodec(HttpRequest.class, new HttpRequestCodec());
        // 设置event bus编解码，用于event-loop解析work-verticle通过event bus传递的数据
        vertx.eventBus().registerDefaultCodec(HttpResponse.class, new HttpResponseCodec());

        ApplicationContext applicationContext = new AnnotationApplicationContext();

        VertxWebContext vertxWebContext = (VertxWebContext) applicationContext.getBean("vertxWebContext");
        

        // 启动work-verticle线程组
        for (int i = 0; i < 16; i++) {
            WorkVerticle workVerticle = new WorkVerticle(applicationContext, vertxWebContext);
            vertx.deployVerticle(workVerticle, new DeploymentOptions()
                    .setWorker(true)
                    .setWorkerPoolName("work-pool")
                    .setMaxWorkerExecuteTimeUnit(TimeUnit.SECONDS)
                    .setMaxWorkerExecuteTime(20));
        }

        // 启动event loop线程组
        for (int i = 0; i < 4; i++) {
            EventLoopVerticle eventLoopVerticle = new EventLoopVerticle(vertxWebContext);
            vertx.deployVerticle(eventLoopVerticle);
        }
    }

    public static void main(String[] args) throws Throwable {
        new VertxWebServer().startServer();
    }
}
