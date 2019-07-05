package com.lee.rokhan.demo.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author lichujun
 * @date 2019/7/5 16:38
 */
@Mapper
public interface DemoMapper {

    String selectUserName();
}
