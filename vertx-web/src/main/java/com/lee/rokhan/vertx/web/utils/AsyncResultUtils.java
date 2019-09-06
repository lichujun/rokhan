package com.lee.rokhan.vertx.web.utils;

import com.lee.rokhan.vertx.web.codec.HttpResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * event bus异步数据转换工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AsyncResultUtils {

    /**
     * 获取异步的响应数据
     * @param msg event bus消息消费后返回的消息数据
     * @param <T> 响应数据的泛型
     * @return 响应数据
     */
    @SuppressWarnings("unchecked")
    public static <T> HttpResponse<T> transResponse(AsyncResult<Message<Object>> msg) {
        return (HttpResponse<T>) msg.result().body();
    }
}
