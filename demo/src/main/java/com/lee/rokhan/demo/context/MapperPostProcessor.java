package com.lee.rokhan.demo.context;

import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.processor.ContextPostProcessor;

/**
 * @author lichujun
 * @date 2019/7/4 17:02
 */
@Component
public class MapperPostProcessor implements ContextPostProcessor {

    @Override
    public void postProcessBeforeInitialization(ApplicationContext context) {

    }

    @Override
    public void postProcessAfterInitialization(ApplicationContext context) {

    }
}
