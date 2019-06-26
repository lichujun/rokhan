package com.lee.rokhan.demo;

import com.lee.rokhan.container.advice.MethodBeforeAdvice;
import com.lee.rokhan.container.advice.MethodReturnAdvice;
import com.lee.rokhan.container.advice.MethodSurroundAdvice;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.advisor.impl.AspectJPointcutAdvisor;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.factory.impl.IocBeanFactory;
import com.lee.rokhan.container.processor.impl.AdvisorAutoProxyCreator;
import java.util.Collections;

/**
 * @author lichujun
 * @date 2019/6/25 13:56
 */
public class DemoServer {

    public static void main(String[] args) throws Throwable {
        IocBeanDefinition iocBeanDefinition = new IocBeanDefinition();
        iocBeanDefinition.setBeanClass(DemoServer.class);

        BeanFactory beanFactory = new IocBeanFactory();

        // 注册切面
        Advisor advisor = new AspectJPointcutAdvisor("demoAdvice", "execution(* com.lee.rokhan.demo.*.doSomething (..))");

        IocBeanDefinition adviceBeanDefinition = new IocBeanDefinition();
        adviceBeanDefinition.setBeanClass(DemoServer.class);
        adviceBeanDefinition.setFactoryMethodName("createReturn");
        beanFactory.registerBeanDefinition("demoAdvice", adviceBeanDefinition);


        beanFactory.registerBeanPostProcessor(new AdvisorAutoProxyCreator(Collections.singletonList(advisor), beanFactory));
        beanFactory.registerBeanDefinition("demo", iocBeanDefinition);
        DemoServer demoServer = (DemoServer) beanFactory.getBean("demo");
        demoServer.doSomething();


    }

    public void doSomething() {
        System.out.println("hello world");
    }

    public void doSome() {
        System.out.println("hello world");
    }

    public static MethodSurroundAdvice createSurround() {
        return (method, args, target) -> {
            System.out.println("你是谁");
            Object returnVal = method.invoke(target, args);
            System.out.println("老子你都不认识了吗，吃屎去");
            return returnVal;
        };
    }

    public static MethodBeforeAdvice createBefore() {
        return ((method, args, target) -> System.out.println("before"));
    }

    public static MethodReturnAdvice createReturn() {
        return ((returnValue, method, args, target) -> {
            System.out.println("return");
        });
    }
}
