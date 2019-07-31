package com.lee.rokhan.mybatis.pool;

import com.lee.rokhan.mybatis.mapping.Environment;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库连接池
 * @author lichujun
 * @date 2019/7/31 14:44
 */
public class DataSourcePool extends AbstractDataSource {

    private final Environment environment;

    public DataSourcePool(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return super.getConnection();
    }
}
