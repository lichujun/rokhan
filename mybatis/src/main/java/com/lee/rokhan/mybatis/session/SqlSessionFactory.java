package com.lee.rokhan.mybatis.session;

import com.lee.rokhan.mybatis.executor.Executor;
import com.lee.rokhan.mybatis.mapping.Configuration;

/**
 * SqlSession工厂
 * @author lichujun
 * @date 2019/7/30 11:42
 */
public class SqlSessionFactory {

    private final Configuration configuration;

    SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public SqlSession openSession() {
        Executor executor = new Executor(configuration);
        return new SqlSession(configuration, executor);
    }
}
