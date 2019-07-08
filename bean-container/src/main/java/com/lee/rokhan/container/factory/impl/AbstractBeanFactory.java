package com.lee.rokhan.container.factory.impl;

import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.common.utils.throwable.ThrowBiConsumer;
import com.lee.rokhan.container.aware.ApplicationContextAware;
import com.lee.rokhan.container.aware.BeanFactoryAware;
import com.lee.rokhan.container.aware.BeanNameAware;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.instance.BeanInstance;
import com.lee.rokhan.container.instance.BeanInstances;
import com.lee.rokhan.container.pojo.BeanReference;
import com.lee.rokhan.container.pojo.PropertyValue;
import com.lee.rokhan.container.processor.BeanPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean工厂抽象类
 * @author lichujun
 * @date 2019/7/6 14:01
 */
@Slf4j
public abstract class AbstractBeanFactory implements BeanFactory, Closeable {

    // 考虑并发情况，默认256，防止扩容
    private static final int DEFAULT_SIZE = 256;

    // 存放Bean对象的容器，一级缓存
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(DEFAULT_SIZE);

    // Bean对象的二级缓存
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(DEFAULT_SIZE);

    // 存放Bean注册信息的容器
    private final Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(DEFAULT_SIZE);

    // Bean初始化前后处理
    private final List<BeanPostProcessor> beanPostProcessors = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
    }


    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        //参数检查
        Objects.requireNonNull(beanName, "beanName不能为空");
        Objects.requireNonNull(beanDefinition, "beanDefinition不能为空");

        // 校验Bean注册信息是否合法
        if (!beanDefinition.validate()) {
            log.error("Bean名称为[{}]的注册信息不合法", beanName);
            throw new RuntimeException("Bean名称为[" + beanName + "]的注册信息不合法");
        }
        // 判断是否已经存在了Bean名称的注册信息，如果有，就停止运行
        else if (containsBeanDefinition(beanName)) {
            log.error("已经存在了Bean名称为[{}]的注册信息", beanName);
            throw new RuntimeException("已经存在了Bean名称为[" + beanName + "]的注册信息");
        } else {
            beanDefinitionMap.put(beanName, beanDefinition);
        }
    }

    @Override
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitionMap.get(beanName);
    }

    @Override
    public boolean containsBeanDefinition(String beanName) {
        return beanDefinitionMap.keySet().contains(beanName);
    }

    /**
     * 执行单例实例的销毁方法
     */
    @Override
    public void close() {
        // 遍历map把bean都取出来然后调用每个bean的销毁方法
        for (Map.Entry<String, BeanDefinition> entry : this.beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.isSingleton() && StringUtils.isNotBlank(beanDefinition.getDestroyMethodName())) {
                Object instance = this.singletonObjects.get(beanName);
                try {
                    Method method = ReflectionUtils.getDeclaredMethod(instance.getClass(), beanDefinition.getDestroyMethodName());
                    if (method != null) {
                        method.invoke(instance);
                    }
                } catch (SecurityException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    log.error("执行bean[" + beanName + "] " + beanDefinition + "的销毁方法异常", e);
                }
            }
        }
    }

    /**
     * Bean初始化前的处理
     */
    private Object applyPostProcessBeforeInitialization(Object bean, String beanName) throws Throwable {
        if (CollectionUtils.isEmpty(beanPostProcessors)) {
            return bean;
        }
        for (BeanPostProcessor bpp : beanPostProcessors) {
            bean = bpp.postProcessBeforeInitialization(bean, beanName);
        }
        return bean;
    }

    /**
     * Bean初始化后的处理
     */
    private Object applyPostProcessAfterInitialization(Object bean, String beanName) throws Throwable {
        if (CollectionUtils.isEmpty(beanPostProcessors)) {
            return bean;
        }
        for (BeanPostProcessor bpp : beanPostProcessors) {
            bean = bpp.postProcessAfterInitialization(bean, beanName);
        }
        return bean;
    }
    /**
     * 对Bean对象进行依赖注入
     * @param beanDefinition Bean注册信息
     * @param beanObject Bean对象
     */
    private void setPropertyDIValues(BeanDefinition beanDefinition, Object beanObject) throws Throwable {
        List<PropertyValue> propertyValues = beanDefinition.getPropertyValues();
        if (CollectionUtils.isEmpty(propertyValues)) {
            return;
        }
        for (PropertyValue propertyValue : propertyValues) {
            String fieldName = propertyValue.getName();
            if (StringUtils.isBlank(fieldName)) {
                continue;
            }
            // 获取Field对象，并设置为可操作
            Class<?> beanClass = beanObject.getClass();
            Field field = beanClass.getDeclaredField(fieldName);
            field.setAccessible(true);

            Object fieldValue = propertyValue.getValue();
            Object realFieldValue;
            if (fieldValue == null) {
                realFieldValue = null;
            }
            // 进行依赖注入
            else if (fieldValue instanceof BeanReference) {
                realFieldValue = getBean(((BeanReference) fieldValue).getBeanName());
            }
            else {
                realFieldValue = fieldValue;
            }
            field.set(beanObject, realFieldValue);
        }
    }

    @Override
    public Object getBean(String beanName) throws Throwable {
        Objects.requireNonNull(beanName, "注册Bean需要输入beanName");
        return doGetBean(beanName);
    }

    /**
     * 生成Bean对象
     * @param beanName Bean名称
     * @return Bean对象
     * @throws Throwable 异常
     */
    private Object doGetBean(String beanName) throws Throwable {
        // 先从Bean对象容器里去取值，如果获取为空，则创建对象
        Object beanObject = singletonObjects.get(beanName);

        // 同步，remove操作造成获取到null，造成空指针异常
        synchronized (this) {
            if (beanObject != null) {
                earlySingletonObjects.remove(beanName);
                return beanObject;
            }
            beanObject = earlySingletonObjects.get(beanName);
            if (beanObject != null) {
                return beanObject;
            }
        }

        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        Objects.requireNonNull(beanDefinition, "Bean名称为" + beanName + "的beanDefinition为空");
        Class<?> beanClass = beanDefinition.getBeanClass();
        // 获取实例生成器
        BeanInstance beanInstance;
        if (beanClass != null) {
            if (StringUtils.isBlank(beanDefinition.getFactoryMethodName())) {
                // 使用构造函数的实例生成器
                beanInstance = BeanInstances.getConstructorInstance();
            } else {
                // 使用工厂方法的实例生成器
                beanInstance = BeanInstances.getFactoryMethodInstance();
            }
        } else {
            // 使用工厂Bean的实例生成器
            beanInstance = BeanInstances.getFactoryBeanInstance();
        }
        // 实例化对象
        beanObject = beanInstance.instance(beanDefinition, this);
        earlySingletonObjects.put(beanName, beanObject);
        // 进行依赖注入
        setPropertyDIValues(beanDefinition, beanObject);
        Class<?> beanObjectClass = beanObject.getClass();
        // 对实现了Aware接口的Bean设置外界属性
        if (BeanNameAware.class.isAssignableFrom(beanObjectClass)) {
            ((BeanNameAware) beanObject).setBeanName(beanName);
        }
        if (BeanFactoryAware.class.isAssignableFrom(beanObjectClass)) {
            ((BeanFactoryAware) beanObject).setBeanFactory(this);
        }
        if (ApplicationContextAware.class.isAssignableFrom(beanObjectClass)) {
            ((ApplicationContextAware) beanObject).setApplicationContext((ApplicationContext) this);
        }
        // 初始化对象之前处理
        beanObject = applyPostProcessBeforeInitialization(beanObject, beanName);
        // 对象初始化
        doInit(beanObject, beanDefinition);
        // 初始化对象之后的处理
        beanObject = applyPostProcessAfterInitialization(beanObject, beanName);
        setLatestDI(beanName, beanObject);
        // 如果是单例模式，则缓存到Map容器
        if (beanDefinition.isSingleton()) {
            singletonObjects.put(beanName, beanObject);
        }
        return beanObject;
    }

    /**
     * 做对象初始化工作
     *
     * @param beanObject     bean实例
     * @param beanDefinition bean注册信息
     */
    private void doInit(Object beanObject, BeanDefinition beanDefinition) throws Throwable {
        if (StringUtils.isNotBlank(beanDefinition.getInitMethodName())) {
            Method method = ReflectionUtils.getDeclaredMethod(beanObject.getClass(), beanDefinition.getInitMethodName());
            if (method != null) {
                method.invoke(beanObject);
            }
        }
    }

    @Override
    public void processAllBeanDefinition(ThrowBiConsumer<String, BeanDefinition, Throwable> throwBiConsumer) throws Throwable {
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            throwBiConsumer.accept(beanDefinitionEntry.getKey(), beanDefinitionEntry.getValue());
        }
    }

    @Override
    public void processAllBean(ThrowBiConsumer<String, Object, Throwable> throwBiConsumer) throws Throwable {
        for (Map.Entry<String, Object> objectEntry : singletonObjects.entrySet()) {
            throwBiConsumer.accept(objectEntry.getKey(), objectEntry.getValue());
        }
    }

    @Override
    public void processAllEarlyBean(ThrowBiConsumer<String, Object, Throwable> throwBiConsumer) throws Throwable {
        for (Map.Entry<String, Object> objectEntry : earlySingletonObjects.entrySet()) {
            throwBiConsumer.accept(objectEntry.getKey(), objectEntry.getValue());
        }
    }

    /**
     * 设置最新依赖
     */
    private void setLatestDI(String diBeanName, Object diBeanObject) throws Throwable {
        processAllEarlyBean((beanName, beanObject) -> {
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (beanDefinition == null) {
                return;
            }
            List<PropertyValue> propertyValues = beanDefinition.getPropertyValues();
            if (CollectionUtils.isEmpty(propertyValues)) {
                return;
            }
            for (PropertyValue propertyValue : propertyValues) {
                String fieldBeanName = propertyValue.getBeanName();
                if (StringUtils.isBlank(fieldBeanName)) {
                    continue;
                }
                if (diBeanName.equals(fieldBeanName)) {
                    ReflectionUtils.setFieldValue(beanObject, propertyValue.getName(), diBeanObject);
                }
            }
        });
    }

}
