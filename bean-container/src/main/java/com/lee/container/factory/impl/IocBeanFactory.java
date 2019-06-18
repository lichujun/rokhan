package com.lee.container.factory.impl;

import com.lee.container.pojo.BeanReference;
import com.lee.container.definition.BeanDefinition;
import com.lee.container.factory.BeanFactory;
import com.lee.container.instance.BeanInstance;
import com.lee.container.instance.impl.BeanInstances;
import com.lee.container.pojo.PropertyValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.Closeable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ioc实例工厂
 * 注：屏蔽构造函数，只能通过工厂方法生成对象
 *
 * @author lichujun
 * @date 2019/6/17 11:38
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class IocBeanFactory implements BeanFactory, Closeable {

    // 考虑并发情况，默认256，防止扩容
    private static final int DEFAULT_SIZE = 256;
    // 存放Bean注册信息的容器
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(DEFAULT_SIZE);
    // 存放Bean对象的容器
    private Map<String, Object> beanMap = new ConcurrentHashMap<>(DEFAULT_SIZE);


    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws Exception {
        //参数检查
        Objects.requireNonNull(beanName, "beanName不能为空");
        Objects.requireNonNull(beanDefinition, "beanDefinition不能为空");

        // 校验Bean注册信息是否合法
        if (!beanDefinition.validate()) {
            log.error("Bean名称为[{}]的注册信息不合法");
            throw new Exception("Bean名称为[" + beanName + "]的注册信息不合法");
        }
        // 判断是否已经存在了Bean名称的注册信息，如果有，就停止运行
        else if (containsBeanDefinition(beanName)) {
            log.error("已经存在了Bean名称为[{}]的注册信息", beanName);
            throw new Exception("已经存在了Bean名称为[" + beanName + "]的注册信息");
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

    @Override
    public Object getBeanByName(String beanName) throws Exception {
        Objects.requireNonNull(beanName, "注册Bean需要输入beanName");
        Object beanObject = doGetBeanWithoutDI(beanName);
        BeanDefinition beanDefinition = getBeanDefinition(beanName);
        setPropertyDIValues(beanDefinition, beanObject);
        return beanObject;
    }

    /**
     * 对Bean对象进行依赖注入
     * @param beanDefinition Bean注册信息
     * @param beanObject Bean对象
     */
    private void setPropertyDIValues(BeanDefinition beanDefinition, Object beanObject) throws Exception {
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
            //
            else if (fieldValue instanceof BeanReference) {
                realFieldValue = doGetBeanWithoutDI(((BeanReference) fieldValue).getBeanName());
            }
            // TODO 其他类型处理，例如配置文件
            else {
                realFieldValue = null;
            }
            field.set(beanObject, realFieldValue);
        }
    }

    /**
     * 生成Bean对象，不进行依赖注入
     * @param beanName Bean名称
     * @return Bean对象
     * @throws Exception 异常
     */
    private Object doGetBeanWithoutDI(String beanName) throws Exception {
        Object beanObject = beanMap.get(beanName);
        if (beanObject != null) {
            return beanObject;
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        Objects.requireNonNull(beanDefinition, "beanDefinition不能为空");

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
        // 对象初始化
        doInit(beanObject, beanDefinition);
        // 如果是单例模式，则缓存到Map容器
        if (beanDefinition.isSingleton()) {
            beanMap.put(beanName, beanObject);
        }
        return beanObject;
    }

    /**
     * 做对象初始化工作
     *
     * @param beanObject     bean实例
     * @param beanDefinition bean注册信息
     */
    private void doInit(Object beanObject, BeanDefinition beanDefinition) throws Exception {
        if (StringUtils.isNotBlank(beanDefinition.getInitMethodName())) {
            Method method = beanObject.getClass().getDeclaredMethod(beanDefinition.getInitMethodName());
            if (method != null) {
                method.invoke(beanObject);
            }
        }
    }

    @Override
    public Object getBeanByClass(Class<?> classObject) throws Exception {
        Objects.requireNonNull(classObject, "类对象不能为空");
        String className = classObject.getSimpleName();
        String beanName = StringUtils.uncapitalize(className);
        return getBeanByName(beanName);
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
                Object instance = this.beanMap.get(beanName);
                try {
                    Method method = instance.getClass().getDeclaredMethod(beanDefinition.getDestroyMethodName());
                    method.invoke(instance);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    log.error("执行bean[" + beanName + "] " + beanDefinition + "的销毁方法异常", e);
                }
            }
        }
    }

    @Override
    public Object[] getArgumentValues(BeanDefinition beanDefinition) throws Exception {
        List<Object> defs = beanDefinition.getArgumentValues();
        if (CollectionUtils.isEmpty(defs)) {
            return null;
        }
        Object[] values = new Object[defs.size()];
        int i = 0;
        //values数组的元素
        Object value;
        for (Object realValue : defs) {
            if (realValue == null) {
                value = null;
            } else if (realValue instanceof BeanReference) {
                value = this.getBeanByName(((BeanReference) realValue).getBeanName());
            } else {
                value = realValue;
            }
            values[i++] = value;
        }
        return values;
    }

}