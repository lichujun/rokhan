package com.lee.rokhan.demo.controller;

import com.lee.rokhan.container.annotation.Autowired;
import com.lee.rokhan.container.annotation.Controller;
import com.lee.rokhan.demo.service.DemoService;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;

import javax.annotation.PostConstruct;

/**
 * @author lichujun
 * @date 2019/6/28 16:23
 */
@Controller
public class DemoController {

    @Autowired
    private DemoService demoService;

    /*DemoController(DemoService demoService) {
        this.demoService = demoService;
    }*/

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    public void test() {
        demoService.test();
        System.out.println("hello world");
    }

    public void test1() {
        System.out.println("controller");
    }
}
