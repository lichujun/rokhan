package com.lee.rokhan.demo.controller;

import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;

/**
 * @author lichujun
 * @date 2019/7/6 18:10
 */
@Controller
@Aspect
public class TestController {

    @Autowired
    private DemoServiceImpl demoService;

    public String test1() {
        return "test1 controller";
    }

    public String test() {
        demoService.test();
        return "test controller";
    }
}
