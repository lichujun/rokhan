package com.lee.rokhan.container.context.impl;

import com.alibaba.fastjson.JSONArray;
import com.lee.rokhan.common.utils.ScanUtils;
import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.container.constants.ApplicationContextConstants;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.factory.impl.IocBeanFactory;
import com.lee.rokhan.container.resource.YamlResource;
import com.lee.rokhan.container.resource.impl.YamlResourceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.util.*;

/**
 * Bean容器
 * @author lichujun
 * @date 2019/6/25 16:30
 */
@Slf4j
public class AnnotationApplicationContext extends IocBeanFactory implements ApplicationContext {

    /**
     * 扫描包扫出来的所有类
     */
    private final Set<Class<?>> classSet;

    /**
     * 配置文件
     */
    private final YamlResource yamlResource;

    /**
     * 初始化classSet和yamlResource
     * @throws IOException 扫描class文件IO异常
     */
    @SuppressWarnings("unchecked")
    public AnnotationApplicationContext() throws IOException {
        yamlResource = new YamlResourceImpl();
        // 获取需要扫描的包
        JSONArray scanPackages = yamlResource.getYamlNodeArrayResource(ApplicationContextConstants.SCAN_PACKAGES);
        // 如果扫描的包为空，则classSet设为空集合
        if (scanPackages == null || scanPackages.isEmpty()) {
            classSet = Collections.EMPTY_SET;
        }
        // 如果扫描的包不为空，则扫描出所有class
        else {
            classSet = new HashSet<>();
            for (Object scanPackage : scanPackages) {
                Set<Class<?>> packageClassSet = ScanUtils.getClasses((String) scanPackage);
                if (CollectionUtils.isNotEmpty(packageClassSet)) {
                    classSet.addAll(packageClassSet);
                }
            }
        }
    }


    /**
     * 初始化Ioc容器
     */
    public void init() {
        if (CollectionUtils.isEmpty(classSet)) {
            return;
        }
        // 1.注册BeanDefinition
        for (Class<?> clazz : classSet) {
            if (clazz == null) {
                continue;
            }
            String beanName = getComponentName(clazz);
            if (StringUtils.isNotBlank(beanName)) {
                IocBeanDefinition iocBeanDefinition = new IocBeanDefinition();
                iocBeanDefinition.setBeanClass(clazz);
                registerBeanDefinition(beanName, iocBeanDefinition);
            }

        }
    }

    /**
     * 获取该类的Bean名称
     * @param clazz 类对象
     * @return Bean名称
     */
    private String getComponentName(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        String beanName = null;
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getDeclaredAnnotation(Component.class);
            beanName = component.value();
        }
        if (clazz.isAnnotationPresent(Controller.class)) {
            Controller controller = clazz.getDeclaredAnnotation(Controller.class);
            beanName = controller.value();
        }
        if (clazz.isAnnotationPresent(Service.class)) {
            Service service = clazz.getDeclaredAnnotation(Service.class);
            beanName = service.value();
        }
        if (clazz.isAnnotationPresent(Repository.class)) {
            Repository repository = clazz.getDeclaredAnnotation(Repository.class);
            beanName = repository.value();
        }
        if (beanName == null) {
            return null;
        }
        if (StringUtils.isBlank(beanName)) {
            return StringUtils.uncapitalize(clazz.getSimpleName());
        }
        return beanName;
    }
}
