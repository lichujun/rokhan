package com.lee.rokhan.mybatis.executor;

import com.lee.rokhan.mybatis.mapping.Configuration;
import com.lee.rokhan.mybatis.pool.DataSourcePool;

import javax.sql.DataSource;

/**
 * @author lichujun
 * @date 2019/7/31 14:33
 */
public class Executor {

    private final DataSource dataSource;

    public Executor(Configuration configuration) {
        dataSource = new DataSourcePool(configuration.getEnvironment());
    }
}
