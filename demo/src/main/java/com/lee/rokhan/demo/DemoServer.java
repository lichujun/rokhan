package com.lee.rokhan.demo;

import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.context.impl.AnnotationApplicationContext;
import com.lee.rokhan.demo.controller.DemoController;
import com.lee.rokhan.demo.controller.TestController;
import com.lee.rokhan.demo.service.impl.DemoServiceImpl;

/**
 * @author lichujun
 * @date 2019/6/25 13:56
 */
public class DemoServer {

    public static void main(String[] args) throws Throwable {

        ApplicationContext context = new AnnotationApplicationContext();
        //((DemoController) context.getBean("demoController")).test();

        //((DemoServiceImpl) context.getBean("demoServiceImpl")).test1();

        /*DemoController demoController = (DemoController) context.getBean("demoController");
        demoController.test();*/
        DemoServiceImpl demoService = ((DemoServiceImpl) context.getBean("demoServiceImpl"));

        TestController testController = (TestController) context.getBean("testController");



        demoService.test1();

        //testController.test1();
    }

}
