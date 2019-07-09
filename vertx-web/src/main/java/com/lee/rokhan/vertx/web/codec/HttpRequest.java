package com.lee.rokhan.vertx.web.codec;

import io.vertx.core.MultiMap;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HttpRequest {

    private List<Object> paramList;

    private MultiMap headers;
}
