package com.lee.rokhan.demo;

import com.lee.rokhan.container.context.impl.AnnotationApplicationContext;

/**
 * @author lichujun
 * @date 2019/6/25 13:56
 */
public class DemoServer {

    public static void main(String[] args) throws Throwable {

        AnnotationApplicationContext context = new AnnotationApplicationContext();
        context.init();
    }

}
