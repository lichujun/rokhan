package com.lee.rokhan.demo.controller;

import com.lee.rokhan.container.advice.MethodBeforeAdvice;
import com.lee.rokhan.container.advice.MethodReturnAdvice;
import com.lee.rokhan.container.advice.MethodSurroundAdvice;
import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.container.aware.ApplicationContextAware;
import com.lee.rokhan.container.aware.BeanFactoryAware;
import com.lee.rokhan.container.aware.BeanNameAware;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.demo.configuration.TestConf;
import com.lee.rokhan.demo.jdbc.SqlSessionFactoryUtils;
import com.lee.rokhan.demo.mapper.DemoMapper;
import com.lee.rokhan.demo.service.DemoService;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;
import javax.annotation.PostConstruct;

/**
 * @author lichujun
 * @date 2019/6/28 16:23
 */
@Controller
@Aspect
public class DemoController implements BeanNameAware, BeanFactoryAware, ApplicationContextAware {

    @Autowired("test")
    private DemoService demoService;

    @Autowired
    private DemoServiceImpl demoService1;

    @Autowired
    private SqlSessionFactoryUtils sqlSessionFactoryUtils;

    @Autowired
    private TestConf testConf;

    @Autowired
    private DemoMapper demoMapper;

   /*DemoController(DemoService demoService) {
        this.demoService = demoService;
    }*/

   private String name = "controller";

    @PostConstruct
    public void init() {
        System.out.println("init");
    }

    public String test() {
        demoService.test();
        System.out.println(demoMapper.selectUserName());
        //mapperInterface.doSome();
        //demoService1.test();
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void test1() {
        System.out.println("controller");
    }

    @Pointcut("execution(* com.lee.rokhan.demo.contr*.*.test (..))")
    public void some() {

    }

    @After("some()")
    public MethodReturnAdvice createReturn() {
        return (returnValue, method, args, target)  -> System.out.println("return " + returnValue);
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

    /*@Bean
    public DemoService getDemoService() {
        return new DemoServiceImpl();
    }*/

    @Override
    public void setBeanName(String beanName) {
        System.out.println(beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws Throwable {
        //DemoController demoController = (DemoController) beanFactory.getBean("demoController");
        //demoController.test();

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        System.out.println(applicationContext.getBeanNamesByType(DemoService.class));
    }
}
