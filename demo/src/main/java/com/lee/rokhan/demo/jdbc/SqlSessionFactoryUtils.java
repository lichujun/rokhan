package com.lee.rokhan.demo.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author lichujun
 * @date 2019/7/5 15:49
 */
@Slf4j
public class SqlSessionFactoryUtils {


    private static String defaultName;

    /**
     * 会话工厂
     */
    private static Map<String, SqlSessionFactory> sqlSessionFactoryMap = new HashMap<>();

    /**
     * 构造器注入
     */
    public SqlSessionFactoryUtils(Map<String, JdbcConfiguration.JdbcConf> jdbcConfMap) {
        init(jdbcConfMap);
    }

    /**
     * 获取连接
     *
     * @return SqlSession
     */
    public SqlSession getSqlSession(String name) {
        SqlSessionFactory sqlSessionFactory;
        // 设置为自动提交事务
        if (StringUtils.isBlank(name)) {
            sqlSessionFactory = sqlSessionFactoryMap.get(defaultName);
            log.debug("获取[{}]的数据库连接", defaultName);
        } else {
            sqlSessionFactory = sqlSessionFactoryMap.get(name);
            if (sqlSessionFactory == null) {
                log.error("找不到{}对应的会话工厂", name);
                sqlSessionFactory = sqlSessionFactoryMap.get(defaultName);
                log.debug("获取[{}]的数据库连接", defaultName);
            } else {
                log.debug("获取[{}]的数据库连接", name);
            }
        }
        return sqlSessionFactory.openSession(true);
    }

    public static String[] getDefaultDatasource() {
        String[] arr = {defaultName};
        return arr;
    }

    private void init(Map<String, JdbcConfiguration.JdbcConf> jdbcConfMap) {
        if (MapUtils.isEmpty(jdbcConfMap)) {
            return;
        }
        int i = 0;
        for (Map.Entry<String, JdbcConfiguration.JdbcConf> jdbcConfEntry : jdbcConfMap.entrySet()) {
            String name = jdbcConfEntry.getKey();
            if (i == 0) {
                defaultName = name;
                i++;
            }
            JdbcConfiguration.JdbcConf jdbcConf = jdbcConfEntry.getValue();
            // 配置事务管理，这里我们使用JDBC的事务
            TransactionFactory trcFactory = new JdbcTransactionFactory();
            // 配置Environment对象，"development"是我们给起的名字
            Environment env = new Environment("development", trcFactory, getDruidPool(jdbcConf));
            // 创建Configuration对象
            Configuration config = new Configuration(env);
            // <settings></settings>中的内容在此处配置
            Optional.ofNullable(jdbcConf.getCacheEnabled())
                    .ifPresent(config::setCacheEnabled);
            Optional.ofNullable(jdbcConf.getUseGeneratedKeys())
                    .ifPresent(config::setUseGeneratedKeys);
            Optional.ofNullable(jdbcConf.getDefaultExecutorType())
                    .ifPresent(config::setDefaultExecutorType);
            if (StringUtils.isNotBlank(jdbcConf.getLogImpl())) {
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends Log> logClass = (Class<? extends Log>) Class.forName(jdbcConf.getLogImpl());
                    config.setLogImpl(logClass);
                } catch (ClassNotFoundException e) {
                    log.warn("设置mybatis的日志发生错误", e);
                }
            }
            if (CollectionUtils.isNotEmpty(jdbcConf.getMappers())) {
                for (String mapper : jdbcConf.getMappers()) {
                    Optional.ofNullable(mapper)
                            .filter(StringUtils::isNotBlank)
                            .ifPresent(config::addMappers);
                }
            }
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(config);
            sqlSessionFactoryMap.put(name, sqlSessionFactory);
        }
    }

    private static DruidDataSource getDruidPool(JdbcConfiguration.JdbcConf jdbcConf) {
        DruidDataSource druid = new DruidDataSource();
        druid.setDriverClassName(jdbcConf.getDriver());
        druid.setUrl(jdbcConf.getUrl());
        druid.setUsername(jdbcConf.getUsername());
        druid.setPassword(jdbcConf.getPassword());
        druid.setDefaultAutoCommit(true);
        druid.setInitialSize(jdbcConf.getInitialSize());
        druid.setMaxActive(jdbcConf.getMaxActive());
        druid.setMaxWait(jdbcConf.getMaxWait());
        druid.setQueryTimeout(jdbcConf.getDefaultQueryTimeoutSeconds());
        druid.setMinIdle(jdbcConf.getMinIdle());
        druid.setMinEvictableIdleTimeMillis(jdbcConf.getMinEvictableIdleTimeMillis());
        druid.setTimeBetweenEvictionRunsMillis(jdbcConf.getTimeBetweenEvictionRunsMillis());
        druid.setTestWhileIdle(jdbcConf.getTestWhileIdle());
        druid.setValidationQuery(jdbcConf.getValidationQuery());
        druid.setRemoveAbandoned(jdbcConf.getRemoveAbandoned());
        druid.setRemoveAbandonedTimeout(jdbcConf.getRemoveAbandonedTimeout());
        return druid;
    }
}
