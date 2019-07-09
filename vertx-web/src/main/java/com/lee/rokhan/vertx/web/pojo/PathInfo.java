package com.lee.rokhan.vertx.web.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lichujun
 * @date 2018/12/13 11:36 PM
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PathInfo {

    /**
     * http请求路径
     */
    private String httpPath;

    /**
     * 处理http请求的方法
     */
    private String httpMethod;
}
