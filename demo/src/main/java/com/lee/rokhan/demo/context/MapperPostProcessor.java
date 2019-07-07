package com.lee.rokhan.demo.context;

import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.pojo.PropertyValue;
import com.lee.rokhan.container.processor.ContextPostProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @author lichujun
 * @date 2019/7/4 17:02
 */
@Component
public class MapperPostProcessor implements ContextPostProcessor {

    @Override
    public void postProcessBeforeInitialization(ApplicationContext context) throws Throwable {
        context.processScanClass(clazz -> {
            if (clazz.isAnnotationPresent(Mapper.class)) {
                MapperInvocationHandler mapperInvocationHandler = new MapperInvocationHandler(clazz);
                Class<?>[] interfaces = {clazz};
                BeanDefinition beanDefinition = new IocBeanDefinition();
                beanDefinition.setReturnType(clazz);
                beanDefinition.setFactoryBeanName("mapperPostProcessor");
                beanDefinition.setFactoryMethodName("getMapperProxy");
                beanDefinition.setArgumentValues(Arrays.asList(mapperInvocationHandler, interfaces));
                String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
                context.registerBeanDefinition(beanName, beanDefinition);
            }
        });

    }

    public Object getMapperProxy(MapperInvocationHandler mapperInvocationHandler, Class<?>[] interfaces) {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, mapperInvocationHandler);
    }

    @Override
    public void postProcessAfterInitialization(ApplicationContext context) {

    }
}
