package com.lee.rokhan.mybatis.session;

import com.lee.rokhan.mybatis.executor.Executor;
import com.lee.rokhan.mybatis.mapping.Configuration;

/**
 * 会话
 * @author lichujun
 * @date 2019/7/31 14:26
 */
public class SqlSession {

    private final Configuration configuration;

    private final Executor executor;

    public SqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }
}
