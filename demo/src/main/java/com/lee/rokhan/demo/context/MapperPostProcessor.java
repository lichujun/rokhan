package com.lee.rokhan.demo.context;

import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.processor.ContextPostProcessor;

/**
 * @author lichujun
 * @date 2019/7/4 17:02
 */
@Component
public class MapperPostProcessor implements ContextPostProcessor {

    @Override
    public void postProcessBeforeInitialization(ApplicationContext context) throws Throwable {
        MapperInvocationHandler mapperInvocationHandler = new MapperInvocationHandler();
        Class<?>[] interfaces = {MapperInterface.class};
        BeanDefinition beanDefinition = new IocBeanDefinition();
        beanDefinition.setInterfaces(interfaces);
        beanDefinition.setInvocationHandler(mapperInvocationHandler);
        context.registerBeanDefinition("testMapper", beanDefinition);
    }

    @Override
    public void postProcessAfterInitialization(ApplicationContext context) {

    }
}
