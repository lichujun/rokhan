package com.lee.rokhan.demo.controller;

import com.lee.rokhan.container.annotation.Autowired;
import com.lee.rokhan.container.annotation.Controller;
import com.lee.rokhan.demo.service.DemoService;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;

/**
 * @author lichujun
 * @date 2019/6/28 16:23
 */
@Controller
public class DemoController {

    @Autowired
    private DemoServiceImpl demoService;

    public void test() {
        demoService.test();
        System.out.println("hello world");
    }
}
