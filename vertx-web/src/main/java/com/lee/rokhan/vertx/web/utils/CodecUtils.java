package com.lee.rokhan.vertx.web.utils;

import com.alibaba.fastjson.JSON;
import io.vertx.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;

/**
 * @author lichujun
 * @date 2019/2/25 10:37 PM
 */
@Slf4j
public class CodecUtils {

    public static <T> void encode(Buffer buffer, T t) {
        try {
            byte[] data = JSON.toJSONBytes(t);
            buffer.appendBytes(data);
        } catch (Exception e) {
            log.warn("将消息实体封装到Buffer用于传输出现异常", e);
        }
    }

    public static <T> T decode(Buffer buffer, Class<? extends T> tClass) {
        try {
            byte[] data = buffer.getBytes();
            return JSON.parseObject(data, tClass);
        } catch (Exception e) {
            log.warn("从buffer中获取传输的消息实体出现异常", e);
            return null;
        }
    }
}
