package com.lee.rokhan.container.context.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lee.rokhan.common.utils.CastUtils;
import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.common.utils.ScanUtils;
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
import com.lee.rokhan.container.pojo.PropertyValue;
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
    private final Map<Class<?>, List<String>> typeToBeanNames = new HashMap<>();

    /**
     * 配置文件
     */
    private final YamlResource yamlResource;

    /**
     * 初始化classSet和yamlResource
     *
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
    public void init() throws Throwable {
        if (CollectionUtils.isEmpty(classSet)) {
            return;
        }
        List<Advisor> advisors = new ArrayList<>();
        for (Class<?> clazz : classSet) {
            PropertyValue propertyValue = getComponentName(clazz);
            String beanName;
            if (propertyValue == null || StringUtils.isBlank(beanName = propertyValue.getName())) {
                continue;
            }
            // 注册未进行依赖注入的BeanDefinition
            BeanDefinition beanDefinition = registerBeanDefinitionWithoutDI(clazz, propertyValue);
            if (propertyValue.getValue() == Configuration.class) {
                registerConfiguration(beanDefinition, clazz);
            } else {
                // 注册依赖关系
                registerDI(clazz, beanDefinition);
                // 添加Advisor增强器，进行方法增强
                addAdvisors(clazz, advisors, beanName);
            }
        }
        registerBeanPostProcessor(new AdvisorAutoProxyCreator(advisors, this));

        // 一次性加载所有单例的Bean对象
        for (Class<?> clazz : classSet) {
            PropertyValue propertyValue = getComponentName(clazz);
            String beanName;
            if (propertyValue == null || StringUtils.isBlank(beanName = propertyValue.getName())) {
                continue;
            }
            BeanDefinition beanDefinition = getBeanDefinition(beanName);
            if (!clazz.isInterface() && beanDefinition != null && beanDefinition.isSingleton()) {
                getBean(beanName);
            }
        }
    }

    /**
     * 注册配置文件
     * @param beanDefinition Bean注册信息
     * @param clazz 类对象
     */
    private void registerConfiguration(BeanDefinition beanDefinition, Class<?> clazz) {
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
                        String fieldJSON = node.getString(field.getName());
                        Object fieldValue = CastUtils.convert(fieldJSON, field.getGenericType(), field.getType());
                        PropertyValue fieldProperty = new PropertyValue(field.getName(), fieldValue);
                        beanDefinition.addPropertyValue(fieldProperty);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getBeanNamesByType(Class<?> type) {
        if (MapUtils.isEmpty(typeToBeanNames)) {
            for (Class<?> clazz : classSet) {
                if (clazz == null) {
                    continue;
                }
                PropertyValue propertyValue = getComponentName(clazz);
                String beanName;
                if (propertyValue == null || StringUtils.isBlank(beanName = propertyValue.getName())) {
                    continue;
                }
                // 获取Bean对象实现的所有接口
                Class<?>[] typeInterfaces = Optional.of(clazz)
                        .map(Class::getInterfaces)
                        .orElse(null);
                if (typeInterfaces == null || ArrayUtils.isEmpty(typeInterfaces)) {
                    continue;
                }
                // 将接口和它的所有实现注册到容器中
                for (Class<?> typeInterface : typeInterfaces) {
                    List<String> beanNames = typeToBeanNames.get(typeInterface);
                    if (beanNames == null) {
                        beanNames = new ArrayList<>();
                        typeToBeanNames.put(type, beanNames);
                    }
                    beanNames.add(beanName);
                }
            }
        }
        return typeToBeanNames.get(type);
    }

    /**
     * 注册Bean的注册信息，不包含依赖关系
     *
     * @param clazz 类对象
     */
    private BeanDefinition registerBeanDefinitionWithoutDI(Class<?> clazz, PropertyValue propertyValue) {
        // 通过构造函数实例化Bean对象注册Bean信息
        BeanDefinition beanDefinition = new IocBeanDefinition();
        beanDefinition.setBeanClass(clazz);
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
        String beanName = propertyValue.getName();
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
        return beanDefinition;
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
                    String pointcutName = method.getName() + "()";
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
            PropertyValue propertyValue = null;
            Class<?> returnType = method.getReturnType();
            if (before != null) {
                if (returnType == MethodBeforeAdvice.class) {
                    propertyValue = new PropertyValue(before.value(), before);
                }
            } else if (after != null) {
                if (returnType == MethodReturnAdvice.class) {
                    propertyValue = new PropertyValue(after.value(), after);
                }
            } else if (around != null) {
                if (returnType == MethodSurroundAdvice.class) {
                    propertyValue = new PropertyValue(around.value(), around);
                }
            }
            if (propertyValue != null) {
                String pointcutName = propertyValue.getName();
                String expression = pointcutMap.get(pointcutName);
                String adviceBeanName = pointcutName + propertyValue.getValue().getClass().getSimpleName();
                Advisor advisor = new AspectJPointcutAdvisor(adviceBeanName, expression);
                advisors.add(advisor);
                BeanDefinition beanDefinition = new IocBeanDefinition();
                beanDefinition.setFactoryBeanName(beanName);
                beanDefinition.setFactoryMethodName(method.getName());
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
                parameterBeanName = getDIValueByType(parameterType);
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
        PropertyValue property = getComponentName(clazz);
        String propertyBeanName;
        if (property == null || StringUtils.isBlank(propertyBeanName = property.getName())) {
            propertyBeanName = StringUtils.uncapitalize(field.getType().getSimpleName());
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
        List<String> beanNames = getBeanNamesByType(type);
        // 没有实现的Bean对象，则抛出异常，停止运行
        if (CollectionUtils.isEmpty(beanNames)) {
            throw new RuntimeException("找不到" + type.getSimpleName() + "的Bean");
        }
        // 如果有多个实现，但是没有指定Bean名称，则抛出异常，停止运行
        else if (beanNames.size() > 1) {
            throw new RuntimeException("该接口" + type.getSimpleName() + "有多个实现，请指定Bean名称");
        }
        return beanNames.get(0);
    }

    /**
     * 获取该类的Bean名称
     *
     * @param clazz 类对象
     * @return Bean名称
     */
    private PropertyValue getComponentName(Class<?> clazz) {
        Object componentValue = null;
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
        return new PropertyValue(componentName, componentValue);
    }

}
