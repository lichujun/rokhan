package com.lee.rokhan.demo;

import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.context.impl.AnnotationApplicationContext;
import com.lee.rokhan.demo.controller.DemoController;

/**
 * @author lichujun
 * @date 2019/6/25 13:56
 */
public class DemoServer {

    public static void main(String[] args) throws Throwable {

        ApplicationContext context = new AnnotationApplicationContext();
        ((DemoController) context.getBean("demoController")).test();
    }

}
