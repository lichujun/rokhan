package com.lee.rokhan.demo.service.impl;

import com.lee.rokhan.container.annotation.Autowired;
import com.lee.rokhan.container.annotation.Bean;
import com.lee.rokhan.container.annotation.Service;
import com.lee.rokhan.demo.controller.DemoController;
import com.lee.rokhan.demo.controller.TestController;
import com.lee.rokhan.demo.service.DemoService;

/**
 * @author lichujun
 * @date 2019/6/28 17:29
 */
@Service
public class DemoServiceImpl extends DemoServiceBaseImpl {

    private Integer age = 12;

    @Autowired
    private TestController testController;


    public void test1() {
        testController.test1();
    }

    /*@Bean(value = "test", initMethod = "test")
    public DemoService test2() {
        return this;
    }*/
}
