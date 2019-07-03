package com.lee.rokhan.demo.configuration;

import com.lee.rokhan.container.annotation.Configuration;

/**
 * @author lichujun
 * @date 2019/7/2 16:29
 */
@Configuration("test1")
public class TestConf {

    private String test;

    public void test() {
        System.out.println("配置test" + test);
    }
}
