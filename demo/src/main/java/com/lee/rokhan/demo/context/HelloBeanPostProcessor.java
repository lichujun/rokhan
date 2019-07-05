package com.lee.rokhan.demo.context;

import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.processor.BeanPostProcessor;
import com.lee.rokhan.demo.controller.DemoController;

/**
 * @author lichujun
 * @date 2019/7/5 10:11
 */
@Component
public class HelloBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws Throwable {
        System.out.println("拦截到Bean名称" + beanName);
        if (bean instanceof DemoController) {
            ((DemoController) bean).setName("老子返回了");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Throwable {
        return bean;
    }
}
