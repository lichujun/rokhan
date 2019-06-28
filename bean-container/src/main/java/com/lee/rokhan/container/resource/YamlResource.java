package com.lee.rokhan.container.resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Yaml资源
 * @author lichujun
 * @date 2019/6/28 15:11
 */
public interface YamlResource extends Resource {

    /**
     * 获取Yaml文件的JSON格式的资源
     * @return JSON格式的资源
     */
    JSONObject getYamlResource();

    /**
     * 获取子节点的资源
     * @param node 子节点的名称
     * @return 子节点的资源
     */
    JSONObject getYamlNodeResource(String node);

    /**
     * 获取子节点的资源
     * @param nodes 子节点的名称数组
     * @return 子节点的资源
     */
    JSONObject getYamlNodeResource(String... nodes);

    /**
     * 获取子节点的资源
     * @param node 子节点的名称
     * @return 子节点的资源
     */
    JSONArray getYamlNodeArrayResource(String node);

    /**
     * 获取子节点的资源
     * @param nodes 子节点的名称数组
     * @return 子节点的资源
     */
    JSONArray getYamlNodeArrayResource(String... nodes);
}
