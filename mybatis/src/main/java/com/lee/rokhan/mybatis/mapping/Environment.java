package com.lee.rokhan.mybatis.mapping;

import lombok.Data;

/**
 * 环境信息
 * @author lichujun
 * @date 2019/7/31 14:15
 */
@Data
public class Environment {

    /**
     * 数据库驱动
     */
    private String driver;

    /**
     * 数据库url
     */
    private String url;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

}
