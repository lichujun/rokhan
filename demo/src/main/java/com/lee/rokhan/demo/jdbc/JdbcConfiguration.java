package com.lee.rokhan.demo.jdbc;

import com.lee.rokhan.container.annotation.Bean;
import com.lee.rokhan.container.annotation.Configuration;
import lombok.Data;
import org.apache.ibatis.session.ExecutorType;
import java.util.List;
import java.util.Map;

import static org.apache.ibatis.session.ExecutorType.REUSE;

/**
 * @author lichujun
 * @date 2019/3/14 14:23
 */
@Data
@Configuration
public class JdbcConfiguration {

    private static Map<String, JdbcConf> datasource;

    @Data
    public static class JdbcConf {
        private String driver;
        private String url;
        private String username;
        private String password;

        private Integer initialSize = 10;
        private Integer maxActive = 100;
        private Integer minIdle = 10;
        private Integer maxIdle = 10;
        private Integer maxWait = 3000;
        private String validationQuery = "SELECT @@VERSION";
        private Boolean testWhileIdle = true;
        private Boolean testOnBorrow = true;
        private Integer timeBetweenEvictionRunsMillis = 30000;
        private Integer minEvictableIdleTimeMillis = 600000;
        private Integer numTestsPerEvictionRun = 3;
        private Boolean removeAbandoned = true;
        private Integer removeAbandonedTimeout = 180;
        private Integer defaultQueryTimeoutSeconds = 30;
        private Integer validationQueryTimeoutSeconds = 5;
        private Boolean testOnReturn = true;

        private Boolean cacheEnabled = true;
        private Boolean useGeneratedKeys = true;
        private ExecutorType defaultExecutorType = REUSE;
        private String logImpl = "org.apache.ibatis.logging.slf4j.Slf4jImpl";
        private List<String> mappers;
    }

    @Bean
    public static SqlSessionFactoryUtils getSqlSessionFactoryUtils() {
        return new SqlSessionFactoryUtils(datasource);
    }
}
