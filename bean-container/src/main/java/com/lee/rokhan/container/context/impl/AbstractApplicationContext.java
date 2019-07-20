package com.lee.rokhan.container.context.impl;

import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.common.utils.ScanUtils;
import com.lee.rokhan.common.utils.throwable.ThrowConsumer;
import com.lee.rokhan.container.advice.Advice;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.annotation.Autowired;
import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.impl.AbstractBeanFactory;
import com.lee.rokhan.container.pojo.BeanReference;
import com.lee.rokhan.container.pojo.ComponentProperty;
import com.lee.rokhan.container.pojo.InjectionProperty;
import com.lee.rokhan.container.pojo.PropertyValue;
import com.lee.rokhan.container.processor.BeanPostProcessor;
import com.lee.rokhan.container.processor.ContextPostProcessor;
import com.lee.rokhan.container.processor.impl.AdvisorAutoProxyCreator;
import com.lee.rokhan.container.proxy.AopProxyFactories;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 应用上下文抽象类
 * @author lichujun
 * @date 2019/7/6 10:24
 */
public abstract class AbstractApplicationContext extends AbstractBeanFactory implements ApplicationContext {

    /**
     * 应用上下文初始化前后增强
     */
    private final List<ContextPostProcessor> contextPostProcessors = new ArrayList<>();

    /**
     * 扫描包扫出来的所有类
     */
    private final Set<Class<?>> classSet = new HashSet<>();

    /**
     * 增强器集合
     */
    private final List<Advisor> advisors = new ArrayList<>();

    /**
     * 接口类型所实现的Bean对象的Bean名称
     */
    private final Map<Class<?>, Set<String>> typeToBeanNames = new ConcurrentHashMap<>(DEFAULT_SIZE);

    /**
     * 组件属性
     */
    private Map<Class<?>, Set<ComponentProperty>> componentPropertyMap = new HashMap<>();

    AbstractApplicationContext() throws Throwable {
        // 初始化扫描所有的Class
        initScanClass();
        // 扫描所有的组件
        scanAllComponent();
        // 扫描所有上下文初始化增强
        scanAllContextPostProcessor();
        // 应用上下文初始化前增强
        initContextBefore();
        // 应用上下文初始化
        initContext();
        // 应用上下文初始化后增强
        initContextAfter();
    }

    @Override
    public void processComponentProperty(ThrowConsumer<ComponentProperty, Throwable> consumer, Class<?> componentClass) throws Throwable {
        if (MapUtils.isEmpty(componentPropertyMap)) {
            return;
        }
        Set<ComponentProperty> componentPropertySet = componentPropertyMap.get(componentClass);
        if (CollectionUtils.isNotEmpty(componentPropertySet)) {
            for (ComponentProperty componentProperty : componentPropertySet) {
                consumer.accept(componentProperty);
            }
        }
    }

    @Override
    public void processAllComponentProperty(ThrowConsumer<ComponentProperty, Throwable> consumer) throws Throwable {
        if (MapUtils.isEmpty(componentPropertyMap)) {
            return;
        }
        for (Map.Entry<Class<?>, Set<ComponentProperty>> componentPropertyEntry : componentPropertyMap.entrySet()) {
            Set<ComponentProperty> componentPropertySet = componentPropertyEntry.getValue();
            if (CollectionUtils.isNotEmpty(componentPropertySet)) {
                for (ComponentProperty componentProperty : componentPropertySet) {
                    consumer.accept(componentProperty);
                }
            }
        }
    }

    @Override
    public Set<String> getBeanNamesByType(Class<?> type) {
        return typeToBeanNames.get(type);
    }

    @Override
    public void processScanClass(ThrowConsumer<Class<?>, Throwable> consumer) throws Throwable {
        for (Class<?> clazz : classSet) {
            consumer.accept(clazz);
        }
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        Class<?> returnType = beanDefinition.getReturnType();
        addTypeToName(beanName, returnType);
        super.registerBeanDefinition(beanName, beanDefinition);
    }

    @Override
    public void registerAdvisor(Advisor advisor) {
        advisors.add(advisor);
    }

    /**
     * 扫描所有包的Class
     * @param packageNames 包名集合
     * @throws Throwable 异常
     */
    @Override
    public void scanClass(Set<String> packageNames) throws Throwable {
        for (String packageName : packageNames) {
            Set<Class<?>> packageClassSet = ScanUtils.getClasses(packageName);
            if (CollectionUtils.isNotEmpty(packageClassSet)) {
                classSet.addAll(packageClassSet);
            }
        }
    }

    /**
     * 添加接口与它的实现的对应关系
     * @param beanName Bean名称
     * @param clazz 接口
     */
    private void addTypeToName(String beanName, Class<?> clazz) {
        if (clazz.isInterface()) {
            Set<String> beanNames = typeToBeanNames.computeIfAbsent(clazz, k -> new HashSet<>());
            beanNames.add(beanName);
        } else {
            // 获取Bean对象实现的所有接口
            Set<Class<?>> typeInterfaces = Optional.of(clazz)
                    .map(ReflectionUtils::getInterfaces)
                    .orElse(null);
            if (typeInterfaces == null || CollectionUtils.isEmpty(typeInterfaces)) {
                return;
            }
            // 将接口和它的所有实现注册到容器中
            for (Class<?> typeInterface : typeInterfaces) {
                Set<String> beanNames = typeToBeanNames.computeIfAbsent(typeInterface, k -> new HashSet<>());
                beanNames.add(beanName);
            }
        }
    }

    /**
     * 应用上下文初始化前增强
     * @throws Throwable 异常
     */
    private void initContextBefore() throws Throwable {
        if (CollectionUtils.isNotEmpty(contextPostProcessors)) {
            for (ContextPostProcessor contextPostProcessor : contextPostProcessors) {
                contextPostProcessor.postProcessBeforeInitialization(this);
            }
        }
    }

    /**
     * 应用上下文初始化后增强
     * @throws Throwable 异常
     */
    private void initContextAfter() throws Throwable {
        if (CollectionUtils.isNotEmpty(contextPostProcessors)) {
            for (ContextPostProcessor contextPostProcessor : contextPostProcessors) {
                contextPostProcessor.postProcessAfterInitialization(this);
            }
        }
    }

    /**
     * 扫描所有上下文初始化增强
     * @throws Throwable 异常
     */
    private void scanAllContextPostProcessor() throws Throwable {
        processComponentProperty(componentProperty -> {
            Class<?> clazz = componentProperty.getClazz();
            if (ContextPostProcessor.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                contextPostProcessors.add((ContextPostProcessor) clazz.newInstance());
            }
        }, Component.class);
    }

    /**
     * 注册所有Bean初始化前后增强
     * @throws Throwable 异常
     */
    private void registerAllBeanPostProcessor() throws Throwable {
        processComponentProperty(componentProperty -> {
            InjectionProperty injectionProperty = componentProperty.getInjectionProperty();
            Class<?> clazz = componentProperty.getClazz();
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                registerBeanPostProcessor((BeanPostProcessor) getBean(injectionProperty.getBeanName()));
            }
        }, Component.class);
    }


    /**
     * 添加依赖关系，并添加AOP增强器
     * @throws Throwable 异常
     */
    private void registerDIAndAddAdvisors() throws Throwable {
        processAllComponentProperty(componentProperty -> {
            registerDIRelationship(componentProperty);
            String beanName = componentProperty.getInjectionProperty().getBeanName();
            Class<?> clazz = componentProperty.getClazz();
            // 添加AOP增强器
            addAdvisors(beanName, clazz);
        });
    }

    /**
     * 注册所有Bean的信息，不包含依赖注入的注册信息
     */
    private void registerAllBeanDefinitionWithoutDI() throws Throwable {
        // 注册Bean的信息
        processAllComponentProperty(componentProperty -> {
            Class<?> clazz = componentProperty.getClazz();
            InjectionProperty injectionProperty = componentProperty.getInjectionProperty();
            if (clazz.isInterface()) {
                return;
            }
            // 注册未进行依赖注入的BeanDefinition
            registerBeanDefinitionWithoutDI(clazz, injectionProperty);
        });
    }

    /**
     * 初始化Ioc容器
     */
    private void initContext() throws Throwable {
        if (CollectionUtils.isEmpty(classSet)) {
            return;
        }
        // 注册Bean的信息
        registerAllBeanDefinitionWithoutDI();
        // 注册依赖关系，并添加增强器
        registerDIAndAddAdvisors();
        // 注册Bean增强，此为AOP增强
        registerBeanPostProcessor(new AdvisorAutoProxyCreator(advisors, this));
        // 注册扫描出的Bean增强
        registerAllBeanPostProcessor();
        // 加载增强器
        loadAdvisors();
    }

    /**
     * 加载所有增强器
     * @throws Throwable 异常
     */
    private void loadAdvisors() throws Throwable {
        processAllBeanDefinition((beanName, beanDefinition) -> {
            if (Advice.class.isAssignableFrom(beanDefinition.getReturnType())) {
                getBean(beanName);
            }
        });
    }

    /**
     * 扫描所有组件
     * @throws Throwable 异常
     */
    private void scanAllComponent() throws Throwable {
        processScanClass(clazz -> {
            InjectionProperty injectionProperty = getComponentPropertyValue(clazz);
            if (injectionProperty != null) {
                ComponentProperty componentProperty = new ComponentProperty(clazz, injectionProperty);
                Class<?> componentClass = injectionProperty.getComponentClass();
                Set<ComponentProperty> componentPropertySet = componentPropertyMap
                        .computeIfAbsent(componentClass, it -> new HashSet<>());
                componentPropertySet.add(componentProperty);
            }
        });
    }

    /**
     * 通过接口注册依赖信息
     *
     * @param field          field字段
     * @param beanDefinition Bean的注册信息
     */
    void registerInterfaceDI(Field field, String beanName, BeanDefinition beanDefinition) {
        Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
        if (autowired == null) {
            return;
        }
        String propertyBeanName = autowired.value();
        if (StringUtils.isBlank(propertyBeanName)) {
            propertyBeanName = getDIValueByType(field.getType());
        }
        registerBeanRelationship(beanDefinition, beanName, field.getName(), propertyBeanName);
    }

    private void registerBeanRelationship(BeanDefinition beanDefinition, String beanName, String propertyName, String propertyBeanName) {
        BeanReference beanReference = new BeanReference(propertyBeanName);
        PropertyValue propertyValue = new PropertyValue(propertyName, beanReference, propertyBeanName);
        beanDefinition.addPropertyValue(propertyValue);
        Set<String> relationships = beanRelationship.computeIfAbsent(beanName, name -> Collections.synchronizedSet(new HashSet<>()));
        relationships.add(propertyBeanName);
    }

    /**
     * 通过类注册依赖信息
     *
     * @param field          field字段
     * @param beanDefinition Bean的注册信息
     */
    void registerClassDI(Field field, String beanName, BeanDefinition beanDefinition) {
        Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
        if (autowired == null) {
            return;
        }
        Class<?> clazz = field.getType();
        InjectionProperty injectionProperty = getComponentPropertyValue(clazz);
        String propertyBeanName = autowired.value();
        if (StringUtils.isBlank(propertyBeanName)) {
            if (injectionProperty == null || StringUtils.isBlank(propertyBeanName = injectionProperty.getBeanName())) {
                propertyBeanName = StringUtils.uncapitalize(field.getType().getSimpleName());
            }
        }
        registerBeanRelationship(beanDefinition, beanName, field.getName(), propertyBeanName);
        AopProxyFactories.getDefaultAopProxyFactory().addClassBeanName(propertyBeanName);
    }

    /**
     * 通过类型获取依赖注入的Bean名称
     *
     * @param type 类型
     * @return Bean名称
     */
    String getDIValueByType(Class<?> type) {
        // 获取接口实现的所有Bean对象的bean名称
        Set<String> beanNames = getBeanNamesByType(type);
        // 没有实现的Bean对象，则抛出异常，停止运行
        if (CollectionUtils.isEmpty(beanNames)) {
            throw new RuntimeException("找不到" + type.getSimpleName() + "的Bean");
        }
        // 如果有多个实现，但是没有指定Bean名称，则抛出异常，停止运行
        else if (beanNames.size() > 1) {
            throw new RuntimeException("该接口" + type.getSimpleName() + "有多个实现，请指定Bean名称");
        }
        return beanNames.toArray(new String[0])[0];
    }

}
