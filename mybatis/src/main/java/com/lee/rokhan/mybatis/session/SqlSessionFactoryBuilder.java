package com.lee.rokhan.mybatis.session;

import com.lee.rokhan.mybatis.mapping.Configuration;

import java.io.InputStream;

/**
 * 构建SqlSession工厂
 * @author lichujun
 * @date 2019/7/30 11:42
 */
public class SqlSessionFactoryBuilder {

    public static SqlSessionFactory build(InputStream config) {
        Configuration configuration = null;
        return new SqlSessionFactory(configuration);
    }
}
