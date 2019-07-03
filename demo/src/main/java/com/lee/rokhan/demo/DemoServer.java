package com.lee.rokhan.demo;

import com.lee.rokhan.container.context.impl.AnnotationApplicationContext;
import com.lee.rokhan.demo.controller.DemoController;

/**
 * @author lichujun
 * @date 2019/6/25 13:56
 */
public class DemoServer {

    public static void main(String[] args) throws Throwable {

        AnnotationApplicationContext context = new AnnotationApplicationContext();
        context.init();

        DemoController demoController = (DemoController) context.getBean("demoController");
        demoController.test();

    }

}
