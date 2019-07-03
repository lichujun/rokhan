package com.lee.rokhan.demo.controller;

import com.lee.rokhan.container.advice.MethodBeforeAdvice;
import com.lee.rokhan.container.advice.MethodReturnAdvice;
import com.lee.rokhan.container.advice.MethodSurroundAdvice;
import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.demo.configuration.TestConf;
import com.lee.rokhan.demo.service.DemoService;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;

/**
 * @author lichujun
 * @date 2019/6/28 16:23
 */
@Controller
@Aspect
public class DemoController {

    @Autowired
    private DemoService demoService;

    /*@Autowired
    private DemoServiceImpl demoService1;*/

    @Autowired
    private TestConf testConf;

   /*DemoController(DemoService demoService) {
        this.demoService = demoService;
    }*/

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    public String test() {
        demoService.test();
        testConf.test();
        System.out.println("hello world");
        return "老子返回了";
    }

    public void test1() {
        System.out.println("controller");
    }

    @Pointcut("execution(* com.lee.rokhan.demo.controller.*.test (..))")
    public void some() {

    }

    @After("some()")
    public MethodReturnAdvice createReturn() {
        return (returnValue, method, args, target)  -> System.out.println("return" + returnValue);
    }

    @Before("some()")
    public MethodBeforeAdvice createBefore() {
        return ((method, args, target) -> System.out.println("before"));
    }

    @Around("some()")
    public MethodSurroundAdvice createAround() {
        return ((method, args, target) -> {
            System.out.println("进来around");
            Object returnValue = method.invoke(target, args);
            System.out.println("结束around");
            return returnValue;
        });
    }

}
