package com.lee.rokhan.mybatis.mapping;

import lombok.Data;

/**
 * mapper数据信息
 * @author lichujun
 * @date 2019/7/31 14:20
 */
@Data
public class Statement {

    private String id;

    private String parameterType;

    private String resultType;

    private String sql;
}
