package com.lee.rokhan.vertx.web.codec;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.Builder;
import lombok.Data;

/**
 * @author lichujun
 * @date 2019/2/25 7:57 PM
 */

@Data
@Builder
public class HttpResponse<T> {

    private HttpResponseStatus status;
    private T response;
}
