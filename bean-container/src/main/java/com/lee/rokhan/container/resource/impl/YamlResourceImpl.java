package com.lee.rokhan.container.resource.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lee.rokhan.container.constants.ApplicationContextConstants;
import com.lee.rokhan.container.constants.ResourceConstants;
import com.lee.rokhan.container.context.impl.AnnotationApplicationContext;
import com.lee.rokhan.container.resource.YamlResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * yaml配置文件
 * @author lichujun
 * @date 2019/6/28 16:58
 */
@Slf4j
public class YamlResourceImpl implements YamlResource {

    private final JSONObject yamlResource;

    public YamlResourceImpl() {
        yamlResource = loadResource();
    }

    /**
     * 加载yaml配置文件
     * @return yaml配置文件
     */
    private JSONObject loadResource() {
        // 将yaml文件加载到Input流
        InputStream yamlStream = AnnotationApplicationContext.class
                .getClassLoader()
                .getResourceAsStream(ResourceConstants.YAML_NAME);
        if (yamlStream == null) {
            log.error("未找到配置文件，请检查配置文件");
            return new JSONObject();
        }
        Yaml yaml = new Yaml();
        // 将yaml文件读取成map
        Map<String, Object> yamlMap = yaml.load(yamlStream);
        // 将map转换成JSON
        JSONObject applicationJSON = new JSONObject(yamlMap);
        try {
            // 获取开发环境变量
            String envProperty = System.getProperty(ApplicationContextConstants.ACTIVE);
            String env = StringUtils.isNotBlank(envProperty)
                    ? envProperty
                    : Optional.of(applicationJSON)
                    .map(it -> it.getJSONObject(ApplicationContextConstants.ENVIRONMENT))
                    .map(it -> it.getString(ApplicationContextConstants.ACTIVE))
                    .orElse(null);
            // 如果开发环境变量不为空，则加载对应的配置文件
            if (StringUtils.isNotBlank(env)) {
                // 设置环境变量
                System.setProperty(ApplicationContextConstants.ACTIVE, env);
                // 加载指定环境的yaml配置文件
                String envPath = ResourceConstants.YAML_PREFIX + env + ResourceConstants.YAML_SUFFIX;
                InputStream yamlEnvStream = AnnotationApplicationContext.class.getClassLoader().getResourceAsStream(envPath);
                Map<String, Object> yamlEnvMap = yaml.load(yamlEnvStream);
                return new JSONObject(yamlEnvMap);
            }
            // 返回application的配置文件
            else {
                return applicationJSON;
            }
        } catch (Throwable e) {
            log.warn("加载active的配置异常");
            return new JSONObject();
        }
    }

    @Override
    public JSONObject getYamlResource() {
        return yamlResource;
    }

    @Override
    public JSONObject getYamlNodeResource(String node) {
        return yamlResource.getJSONObject(node);
    }

    @Override
    public JSONObject getYamlNodeResource(String... nodes) {
        if (ArrayUtils.isEmpty(nodes)) {
            return null;
        }
        JSONObject nodeResource = null;
        for (String node : nodes) {
            if (yamlResource == null) {
                break;
            }
            nodeResource = yamlResource.getJSONObject(node);
        }
        return nodeResource;
    }

    @Override
    public JSONArray getYamlNodeArrayResource(String node) {
        return yamlResource.getJSONArray(node);
    }

    @Override
    public JSONArray getYamlNodeArrayResource(String... nodes) {
        if (ArrayUtils.isEmpty(nodes)) {
            return null;
        }
        JSONArray nodeResource = null;
        for (String node : nodes) {
            if (yamlResource == null) {
                break;
            }
            nodeResource = yamlResource.getJSONArray(node);
        }
        return nodeResource;
    }
}
