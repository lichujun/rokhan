package com.lee.rokhan.demo.service.impl;

import com.lee.rokhan.demo.annotation.AopTest;
import com.lee.rokhan.demo.service.DemoService;

public class DemoServiceBaseImpl implements DemoService {


    @Override
    @AopTest
    public void test() {
        System.out.println("service");
    }
}
