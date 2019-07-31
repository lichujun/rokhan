package com.lee.rokhan.mybatis.mapping;

import lombok.Data;
import java.util.Map;

/**
 * Mybatis的配置
 * @author lichujun
 * @date 2019/7/31 14:14
 */
@Data
public class Configuration {

    private Environment environment;

    private Map<String, Statement> statementMap;
}
