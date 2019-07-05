package com.lee.rokhan.container.context.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.common.utils.ScanUtils;
import com.lee.rokhan.common.utils.throwable.ThrowConsumer;
import com.lee.rokhan.container.advice.MethodBeforeAdvice;
import com.lee.rokhan.container.advice.MethodReturnAdvice;
import com.lee.rokhan.container.advice.MethodSurroundAdvice;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.advisor.impl.AspectJPointcutAdvisor;
import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.constants.ApplicationContextConstants;
import com.lee.rokhan.container.constants.ResourceConstants;
import com.lee.rokhan.container.context.ApplicationContext;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.factory.impl.IocBeanFactory;
import com.lee.rokhan.container.pojo.BeanReference;
import com.lee.rokhan.container.pojo.ComponentInjection;
import com.lee.rokhan.container.pojo.ComponentProperty;
import com.lee.rokhan.container.pojo.PropertyValue;
import com.lee.rokhan.container.processor.BeanPostProcessor;
import com.lee.rokhan.container.processor.ContextPostProcessor;
import com.lee.rokhan.container.processor.impl.AdvisorAutoProxyCreator;
import com.lee.rokhan.container.proxy.AopProxyFactories;
import com.lee.rokhan.container.resource.YamlResource;
import com.lee.rokhan.container.resource.impl.YamlResourceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bean容器
 *
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
     * 接口类型所实现的Bean对象的Bean名称
     */
    private final Map<Class<?>, Set<String>> typeToBeanNames = new ConcurrentHashMap<>();

    /**
     * 组件属性
     */
    private final Map<Class<?>, Set<ComponentProperty>> componentPropertyMap;

    /**
     * 配置文件
     */
    private final YamlResource yamlResource;

    /**
     * 初始化classSet和yamlResource
     *
     * @throws IOException 扫描class文件IO异常
     */
    public AnnotationApplicationContext() throws Throwable {
        // 加载Yaml配置文件
        yamlResource = new YamlResourceImpl();
        // 扫描所有的Class
        classSet = scanAllClass();
        // 扫描所有的组件
        componentPropertyMap = scanAllComponent();
        // 扫描所有上下文初始化增强
        List<ContextPostProcessor> contextPostProcessors = scanAllContextPostProcessor();
        // 应用上下文初始化前增强
        initContextBefore(contextPostProcessors);
        // 应用上下文初始化
        initContext();
        // 应用上下文初始化后增强
        initContextAfter(contextPostProcessors);
    }

    @Override
    public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        Class<?> returnType = beanDefinition.getReturnType();
        addTypeToName(beanName, returnType);
        super.registerBeanDefinition(beanName, beanDefinition);
    }

    /**
     * 初始化Ioc容器
     */
    private void initContext() throws Throwable {
        if (CollectionUtils.isEmpty(classSet)) {
            return;
        }
        List<Advisor> advisors = new ArrayList<>();
        // 注册Bean的信息
        processAllComponentProperty(componentProperty -> {
            Class<?> clazz = componentProperty.getClazz();
            ComponentInjection componentInjection = componentProperty.getComponentInjection();
            if (clazz.isInterface()) {
                return;
            }
            // 注册未进行依赖注入的BeanDefinition
            registerBeanDefinitionWithoutDI(clazz, componentInjection);
        });

        // 注册依赖关系
        processAllComponentProperty(componentProperty -> {
            Class<?> clazz = componentProperty.getClazz();
            ComponentInjection componentInjection = componentProperty.getComponentInjection();
            String beanName = componentInjection.getBeanName();
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            Constructor[] constructors = clazz.getDeclaredConstructors();
            if (ArrayUtils.isNotEmpty(constructors)) {
                if (constructors.length > 1) {
                    throw new RuntimeException(clazz.getSimpleName() + "存在多个构造函数");
                }
                Constructor constructor = constructors[0];
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (ArrayUtils.isNotEmpty(parameterTypes)) {
                    List<Object> parameters = getParameterDIValues(parameterTypes);
                    beanDefinition.setArgumentValues(parameters);
                    beanDefinition.setConstructor(constructor);
                }
            }
            if (componentInjection.getComponentClass() == Configuration.class) {
                registerConfiguration(clazz, beanDefinition);
            } else {
                registerDI(clazz, beanDefinition);
                // 添加Advisor增强器，进行方法增强
                addAdvisors(clazz, advisors, beanName);
            }
        });

        // 注册Bean增强，此为AOP增强
        registerBeanPostProcessor(new AdvisorAutoProxyCreator(advisors, this));
        // 注册扫描出的Bean增强
        processComponentProperty(componentProperty -> {
            ComponentInjection componentInjection = componentProperty.getComponentInjection();
            Class<?> clazz = componentProperty.getClazz();
            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                registerBeanPostProcessor((BeanPostProcessor) getBean(componentInjection.getBeanName()));
            }
        }, Component.class);

        // 一次性加载所有单例的Bean对象
        ThrowConsumer<Class<?>, Throwable> loadSingletonBeanConsumer = clazz -> {
            ComponentInjection componentInjection = getComponentPropertyValue(clazz);
            String beanName;
            if (componentInjection == null || StringUtils.isBlank(beanName = componentInjection.getBeanName())) {
                return;
            }
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (!clazz.isInterface() && beanDefinition != null && beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        };
        processScanClass(loadSingletonBeanConsumer);
    }

    /**
     * 扫描所有上下文初始化增强
     * @return 所有上下文初始化增强
     * @throws Throwable 异常
     */
    private List<ContextPostProcessor> scanAllContextPostProcessor() throws Throwable {
        List<ContextPostProcessor> contextPostProcessors = new ArrayList<>();
        processComponentProperty(componentProperty -> {
            Class<?> clazz = componentProperty.getClazz();
            if (ContextPostProcessor.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                contextPostProcessors.add((ContextPostProcessor) clazz.newInstance());
            }
        }, Component.class);
        return contextPostProcessors;
    }

    /**
     * 应用上下文初始化前增强
     * @throws Throwable 异常
     */
    private void initContextBefore(List<ContextPostProcessor> contextPostProcessors) throws Throwable {
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
    private void initContextAfter(List<ContextPostProcessor> contextPostProcessors) throws Throwable {
        if (CollectionUtils.isNotEmpty(contextPostProcessors)) {
            for (ContextPostProcessor contextPostProcessor : contextPostProcessors) {
                contextPostProcessor.postProcessAfterInitialization(this);
            }
        }
    }


    @Override
    public void processScanClass(ThrowConsumer<Class<?>, Throwable> consumer) throws Throwable {
        for (Class<?> clazz : classSet) {
            consumer.accept(clazz);
        }
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
     * 注册配置文件
     * @param beanDefinition Bean注册信息
     * @param clazz 类对象
     */
    private void registerConfiguration(Class<?> clazz, BeanDefinition beanDefinition) {
        if (beanDefinition != null) {
            String configurationNode = clazz.getDeclaredAnnotation(Configuration.class).value();
            JSONObject node;
            if (StringUtils.isBlank(configurationNode)) {
                node = yamlResource.getYamlNodeResource();
            } else {
                String[] nodes = configurationNode.split(ResourceConstants.SPOT);
                node = yamlResource.getYamlNodeResource(nodes);
            }
            if (node != null) {
                Set<Field> fields = ReflectionUtils.getDeclaredFields(clazz);
                for (Field field : fields) {
                    if (!field.isAnnotationPresent(Autowired.class)) {
                        Object fieldValue = node.getObject(field.getName(), field.getGenericType());
                        PropertyValue fieldProperty = new PropertyValue(field.getName(), fieldValue);
                        beanDefinition.addPropertyValue(fieldProperty);
                    }
                }
            }
        }
    }

    /**
     * 扫描所有组件
     * @throws Throwable 异常
     */
    private Map<Class<?>, Set<ComponentProperty>> scanAllComponent() throws Throwable {
        Map<Class<?>, Set<ComponentProperty>> componentPropertyMap = new HashMap<>();
        processScanClass(clazz -> {
            ComponentInjection componentInjection = getComponentPropertyValue(clazz);
            if (componentInjection != null) {
                ComponentProperty componentProperty = new ComponentProperty(clazz, componentInjection);
                Class<?> componentClass = componentInjection.getComponentClass();
                Set<ComponentProperty> componentPropertySet = componentPropertyMap
                        .computeIfAbsent(componentClass, it -> new HashSet<>());
                componentPropertySet.add(componentProperty);
            }
        });
        return componentPropertyMap;
    }

    private Set<Class<?>> scanAllClass() throws IOException {
        // 获取需要扫描的包
        JSONArray scanPackages = yamlResource.getYamlNodeArrayResource(ApplicationContextConstants.SCAN_PACKAGES);
        // 如果扫描的包为空，则classSet设为空集合
        if (scanPackages == null || scanPackages.isEmpty()) {
            return null;
        }
        // 如果扫描的包不为空，则扫描出所有class
        else {
            Set<Class<?>> classSet = new HashSet<>();
            for (Object scanPackage : scanPackages) {
                Set<Class<?>> packageClassSet = ScanUtils.getClasses((String) scanPackage);
                if (CollectionUtils.isNotEmpty(packageClassSet)) {
                    classSet.addAll(packageClassSet);
                }
            }
            return classSet;
        }
    }

    /**
     * 注册Bean的注册信息，不包含依赖关系
     *
     * @param clazz 类对象
     */
    private void registerBeanDefinitionWithoutDI(Class<?> clazz, ComponentInjection componentInjection) {
        // 通过构造函数实例化Bean对象注册Bean信息
        BeanDefinition beanDefinition = new IocBeanDefinition();
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setReturnType(clazz);
        String beanName = componentInjection.getBeanName();
        // 注册Bean的信息
        registerBeanDefinition(beanName, beanDefinition);

        // 通过工厂Bean的方法或者静态工厂方法注册Bean信息
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Bean bean = method.getDeclaredAnnotation(Bean.class);
            if (bean != null) {
                Class<?> returnType = method.getReturnType();
                String beanValue = bean.value();
                if (StringUtils.isBlank(beanValue)) {
                    beanValue = StringUtils.uncapitalize(returnType.getSimpleName());
                }
                BeanDefinition methodBeanDefinition = new IocBeanDefinition();
                // 静态工厂方法注册Bean信息
                if (Modifier.isStatic(method.getModifiers())) {
                    methodBeanDefinition.setBeanClass(clazz);
                    methodBeanDefinition.setFactoryMethodName(method.getName());
                }
                // 工厂Bean的方法注册Bean信息
                else {
                    methodBeanDefinition.setFactoryBeanName(beanName);
                    methodBeanDefinition.setFactoryMethodName(method.getName());
                }
                methodBeanDefinition.setReturnType(returnType);
                // 注册init方法和destroy方法
                String initMethod = bean.initMethod();
                String destroyMethod = bean.destroyMethod();
                if (StringUtils.isNotBlank(initMethod)) {
                    methodBeanDefinition.setInitMethodName(initMethod);
                }
                if (StringUtils.isNotBlank(destroyMethod)) {
                    methodBeanDefinition.setDestroyMethodName(destroyMethod);
                }
                registerBeanDefinition(beanValue, methodBeanDefinition);
            }
            // 注册init方法和destroy方法
            registerInitAndDestroy(beanDefinition, method);
        }
    }

    /**
     * 添加Advisor增强器
     *
     * @param clazz    类对象
     * @param advisors Advisor增强器集合
     */
    private void addAdvisors(Class<?> clazz, List<Advisor> advisors, String beanName) {
        Aspect aspect = clazz.getDeclaredAnnotation(Aspect.class);
        if (aspect == null) {
            return;
        }
        Set<Method> methods = ReflectionUtils.getDeclaredMethods(clazz);
        if (CollectionUtils.isEmpty(methods)) {
            return;
        }
        // 获取所有切点
        Map<String, String> pointcutMap = null;
        for (Method method : methods) {
            Pointcut pointcut = method.getDeclaredAnnotation(Pointcut.class);
            if (pointcut != null) {
                String expression = pointcut.value();
                if (StringUtils.isNotBlank(expression)) {
                    String pointcutName = method.getName() + ApplicationContextConstants.BRACKETS;
                    if (pointcutMap == null) {
                        pointcutMap = new HashMap<>();
                    }
                    pointcutMap.put(pointcutName, expression);
                }
            }
        }
        if (MapUtils.isEmpty(pointcutMap)) {
            return;
        }
        // 进行方法增强
        for (Method method : methods) {
            Before before = method.getDeclaredAnnotation(Before.class);
            After after = method.getDeclaredAnnotation(After.class);
            Around around = method.getDeclaredAnnotation(Around.class);
            String pointcutName = null;
            String adviceType = null;
            Class<?> returnType = method.getReturnType();
            if (before != null) {
                if (returnType == MethodBeforeAdvice.class) {
                    pointcutName = before.value();
                    adviceType = Before.class.getSimpleName();
                }
            } else if (after != null) {
                if (returnType == MethodReturnAdvice.class) {
                    pointcutName = after.value();
                    adviceType = After.class.getSimpleName();
                }
            } else if (around != null) {
                if (returnType == MethodSurroundAdvice.class) {
                    pointcutName = around.value();
                    adviceType = Around.class.getSimpleName();
                }
            }
            if (adviceType != null) {
                String expression = pointcutMap.get(pointcutName);
                String adviceBeanName = pointcutName + adviceType;
                Advisor advisor = new AspectJPointcutAdvisor(adviceBeanName, expression);
                advisors.add(advisor);
                BeanDefinition beanDefinition = new IocBeanDefinition();
                beanDefinition.setFactoryBeanName(beanName);
                beanDefinition.setFactoryMethodName(method.getName());
                beanDefinition.setReturnType(returnType);
                registerBeanDefinition(adviceBeanName, beanDefinition);
            }
        }
    }

    /**
     * 注册init方法和destroy方法
     *
     * @param beanDefinition Bean注册信息
     * @param method         方法
     */
    private void registerInitAndDestroy(BeanDefinition beanDefinition, Method method) {
        PostConstruct postConstruct = method.getDeclaredAnnotation(PostConstruct.class);
        if (postConstruct != null) {
            if (ArrayUtils.isNotEmpty(method.getParameterTypes())) {
                throw new RuntimeException("初始化方法参数列表需要为空");
            }
            beanDefinition.setInitMethodName(method.getName());
        }
        PreDestroy preDestroy = method.getDeclaredAnnotation(PreDestroy.class);
        if (preDestroy != null) {
            if (ArrayUtils.isNotEmpty(method.getParameterTypes())) {
                throw new RuntimeException("销毁方法参数列表需要为空");
            }
            beanDefinition.setDestroyMethodName(method.getName());
        }
    }

    /**
     * 注册Bean的依赖关系
     *
     * @param clazz 类对象
     */
    private void registerDI(Class<?> clazz, BeanDefinition iocBeanDefinition) {
        if (iocBeanDefinition == null) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        if (ArrayUtils.isNotEmpty(fields)) {
            for (Field field : fields) {
                // 如果是接口，则通过接口注册依赖信息
                if (field.getType().isInterface()) {
                    registerInterfaceDI(field, iocBeanDefinition);
                }
                // 如果是类，则通过类注册依赖信息
                else {
                    registerClassDI(field, iocBeanDefinition);
                }
            }
        }
        // Bean对象的方法或静态方法注册参数依赖关系
        Method[] methods = clazz.getDeclaredMethods();
        if (ArrayUtils.isNotEmpty(methods)) {
            for (Method method : methods) {
                Bean bean = method.getDeclaredAnnotation(Bean.class);
                if (bean == null) {
                    continue;
                }
                String beanValue = bean.value();
                if (StringUtils.isBlank(beanValue)) {
                    beanValue = StringUtils.uncapitalize(method.getReturnType().getSimpleName());
                }
                BeanDefinition beanDefinition = getBeanDefinition(beanValue);
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (ArrayUtils.isNotEmpty(parameterTypes)) {
                    List<Object> parameters = getParameterDIValues(parameterTypes);
                    beanDefinition.setArgumentValues(parameters);
                }
            }
        }
    }

    /**
     * 通过参数列表类型获取参数的注入的对象
     *
     * @param parameterTypes 参数列表类型
     * @return 参数列表对象
     */
    private List<Object> getParameterDIValues(Class<?>[] parameterTypes) {
        List<Object> parameters = new ArrayList<>();
        for (Class<?> parameterType : parameterTypes) {
            String parameterBeanName;
            Autowired autowired = parameterType.getDeclaredAnnotation(Autowired.class);
            if (autowired == null || StringUtils.isBlank(autowired.value())) {
                if (parameterType.isInterface()) {
                    parameterBeanName = getDIValueByType(parameterType);
                } else {
                    parameterBeanName = StringUtils.uncapitalize(parameterType.getSimpleName());
                }
            } else {
                parameterBeanName = autowired.value();
            }
            BeanReference beanReference = new BeanReference(parameterBeanName);
            parameters.add(beanReference);
        }
        return parameters;
    }


    /**
     * 通过接口注册依赖信息
     *
     * @param field          field字段
     * @param beanDefinition Bean的注册信息
     */
    private void registerInterfaceDI(Field field, BeanDefinition beanDefinition) {
        Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
        if (autowired == null) {
            return;
        }
        String propertyBeanName = autowired.value();
        if (StringUtils.isBlank(propertyBeanName)) {
            propertyBeanName = getDIValueByType(field.getType());
        }
        BeanReference beanReference = new BeanReference(propertyBeanName);
        PropertyValue propertyValue = new PropertyValue(field.getName(), beanReference);
        beanDefinition.addPropertyValue(propertyValue);
    }

    /**
     * 通过类注册依赖信息
     *
     * @param field          field字段
     * @param beanDefinition Bean的注册信息
     */
    private void registerClassDI(Field field, BeanDefinition beanDefinition) {
        Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
        if (autowired == null) {
            return;
        }
        Class<?> clazz = field.getType();
        ComponentInjection componentInjection = getComponentPropertyValue(clazz);
        String propertyBeanName = autowired.value();
        if (StringUtils.isBlank(propertyBeanName)) {
            if (componentInjection == null || StringUtils.isBlank(propertyBeanName = componentInjection.getBeanName())) {
                propertyBeanName = StringUtils.uncapitalize(field.getType().getSimpleName());
            }
        }
        BeanReference beanReference = new BeanReference(propertyBeanName);
        PropertyValue propertyValue = new PropertyValue(field.getName(), beanReference);
        beanDefinition.addPropertyValue(propertyValue);
        AopProxyFactories.getDefaultAopProxyFactory().addClassBeanName(propertyBeanName);
    }

    /**
     * 通过类型获取依赖注入的Bean名称
     *
     * @param type 类型
     * @return Bean名称
     */
    private String getDIValueByType(Class<?> type) {
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

    /**
     * 获取该类的Bean名称
     *
     * @param clazz 类对象
     * @return Bean名称
     */
    private ComponentInjection getComponentPropertyValue(Class<?> clazz) {
        Class<?> componentValue = null;
        String componentName = null;
        if (clazz.isAnnotationPresent(Component.class)) {
            Component component = clazz.getDeclaredAnnotation(Component.class);
            componentName = component.value();
            componentValue = Component.class;
        }
        if (clazz.isAnnotationPresent(Controller.class)) {
            Controller controller = clazz.getDeclaredAnnotation(Controller.class);
            componentName = controller.value();
            componentValue = Controller.class;
        }
        if (clazz.isAnnotationPresent(Service.class)) {
            Service service = clazz.getDeclaredAnnotation(Service.class);
            componentName = service.value();
            componentValue = Service.class;
        }
        if (clazz.isAnnotationPresent(Repository.class)) {
            Repository repository = clazz.getDeclaredAnnotation(Repository.class);
            componentName = repository.value();
            componentValue = Repository.class;
        }
        if (clazz.isAnnotationPresent(Configuration.class)) {
            Configuration configuration = clazz.getDeclaredAnnotation(Configuration.class);
            componentName = configuration.value();
            componentValue = Configuration.class;
        }
        if (componentValue == null) {
            return null;
        }
        if (StringUtils.isBlank(componentName)) {
            componentName = StringUtils.uncapitalize(clazz.getSimpleName());
        }
        return new ComponentInjection(componentName, componentValue);
    }
}
