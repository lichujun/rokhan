package com.lee.rokhan.vertx.web.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author lichujun
 * @date 2019/6/12 16:11
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MethodParamsWithHeaders {

    private Map<String, MethodParam> methodParameter;

    private Integer headersPosition;
}
