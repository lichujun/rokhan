package com.lee.rokhan.demo.service.impl;

import com.lee.rokhan.container.annotation.Autowired;
import com.lee.rokhan.container.annotation.Bean;
import com.lee.rokhan.container.annotation.Service;
import com.lee.rokhan.demo.controller.DemoController;
import com.lee.rokhan.demo.service.DemoService;

/**
 * @author lichujun
 * @date 2019/6/28 17:29
 */
@Service
public class DemoServiceImpl implements DemoService {

    //@Autowired
    private DemoController demoController;


    @Override
    public void test() {
        System.out.println("service");
    }

    public void test1() {
        demoController.test1();
    }

    @Bean(value = "test", initMethod = "test")
    public DemoService test2() {
        return this;
    }
}
