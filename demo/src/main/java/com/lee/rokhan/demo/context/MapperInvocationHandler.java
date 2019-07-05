package com.lee.rokhan.demo.context;

import com.lee.rokhan.demo.jdbc.JdbcConfiguration;
import lombok.AllArgsConstructor;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/7/4 19:06
 */
@AllArgsConstructor
public class MapperInvocationHandler implements InvocationHandler {

    private Class<?> clazz;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try (SqlSession sqlSession = JdbcConfiguration.getSqlSessionFactoryUtils().getSqlSession("master")) {
            Object mapper = sqlSession.getMapper(clazz);
            return method.invoke(mapper, args);
        }
    }
}
