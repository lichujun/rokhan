package com.lee.rokhan.demo.controller;

import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;
import com.lee.rokhan.vertx.web.annotation.Header;
import com.lee.rokhan.vertx.web.annotation.RequestMapping;
import com.lee.rokhan.vertx.web.pojo.RequestMethod;
import io.vertx.core.MultiMap;
import lombok.Data;

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

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public TestController test() {
        demoService.test();
        return this;
    }
}
