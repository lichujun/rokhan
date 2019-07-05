package com.lee.rokhan.demo.context;

import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.processor.ContextPostProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;

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
                beanDefinition.setInterfaces(interfaces);
                beanDefinition.setReturnType(clazz);
                beanDefinition.setInvocationHandler(mapperInvocationHandler);
                String beanName = StringUtils.uncapitalize(clazz.getSimpleName());
                context.registerBeanDefinition(beanName, beanDefinition);
            }
        });

    }

    @Override
    public void postProcessAfterInitialization(ApplicationContext context) {

    }
}
