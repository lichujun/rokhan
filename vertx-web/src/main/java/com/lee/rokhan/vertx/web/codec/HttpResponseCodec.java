package com.lee.rokhan.vertx.web.codec;

import com.lee.rokhan.vertx.web.utils.CodecUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lichujun
 * @date 2019/2/25 7:57 PM
 */

@Slf4j
public class HttpResponseCodec implements MessageCodec<HttpResponse, HttpResponse> {
    /**
     * 将消息实体封装到Buffer用于传输
     *
     * 实现方式：
     * 使用对象流从对象中获取Byte数组然后追加到Buffer
     */
    @Override
    public void encodeToWire(Buffer buffer, HttpResponse s) {
        CodecUtils.encode(buffer, s);
    }

    /**
     * 从buffer中获取传输的消息实体
     */
    @Override
    public HttpResponse decodeFromWire(int pos, Buffer buffer) {
        return CodecUtils.decode(buffer, HttpResponse.class);
    }

    /**
     * 如果是本地消息则直接返回
     */
    @Override
    public HttpResponse transform(HttpResponse s) {
        return s;
    }

    /**
     * 编解码器的名称：
     * 必须唯一，用于发送消息时识别编解码器，以及取消编解码器
     */
    @Override
    public String name() {
        return "HttpResponseCodec";
    }

    /**
     * 用于识别是否是用户编码器
     * 自定义编解码器通常使用-1
     */
    @Override
    public byte systemCodecID() {
        return -1;
    }
}

