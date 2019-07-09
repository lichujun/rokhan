package com.lee.rokhan.container.context.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.container.advice.MethodBeforeAdvice;
import com.lee.rokhan.container.advice.MethodReturnAdvice;
import com.lee.rokhan.container.advice.MethodSurroundAdvice;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.advisor.impl.AspectJPointcutAdvisor;
import com.lee.rokhan.container.annotation.*;
import com.lee.rokhan.container.annotation.Component;
import com.lee.rokhan.container.constants.ApplicationContextConstants;
import com.lee.rokhan.container.constants.ResourceConstants;
import com.lee.rokhan.container.definition.BeanDefinition;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.pojo.BeanReference;
import com.lee.rokhan.container.pojo.InjectionProperty;
import com.lee.rokhan.container.pojo.ComponentProperty;
import com.lee.rokhan.container.pojo.PropertyValue;
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
import java.util.*;
import java.util.List;

/**
 * 使用yaml配置文件,使用注解的应用上下文
 *
 * @author lichujun
 * @date 2019/6/25 16:30
 */
@Slf4j
public class AnnotationApplicationContext extends AbstractApplicationContext {

    /**
     * 配置文件
     */
    private YamlResource yamlResource;

    public AnnotationApplicationContext() throws Throwable {
    }

    /**
     * 注册依赖关系
     * @param componentProperty 组件
     */
    @Override
    public void registerDIRelationship(ComponentProperty componentProperty) {
        Class<?> clazz = componentProperty.getClazz();
        InjectionProperty injectionProperty = componentProperty.getInjectionProperty();
        String beanName = injectionProperty.getBeanName();
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
        if (injectionProperty.getComponentClass() == Configuration.class) {
            registerConfiguration(clazz, beanDefinition);
        }
        registerBeanAndMethodDI(clazz, beanDefinition);
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
                        PropertyValue fieldProperty = new PropertyValue(field.getName(), fieldValue, null);
                        beanDefinition.addPropertyValue(fieldProperty);
                    }
                }
            }
        }
    }

    /**
     * 通过yaml文件扫描所有Class文件
     * @throws IOException IO异常
     */
    @Override
    public void initScanClass() throws Throwable {
        // 加载Yaml配置文件
        yamlResource = new YamlResourceImpl();
        // 获取需要扫描的包
        JSONArray scanPackages = yamlResource.getYamlNodeArrayResource(ApplicationContextConstants.SCAN_PACKAGES);
        // 如果扫描的包为空，则classSet设为空集合
        Set<String> packageNames;
        if (scanPackages != null && !scanPackages.isEmpty()) {
             packageNames = new HashSet<>(scanPackages.toJavaList(String.class));
            // 添加默认扫描包
            packageNames.addAll(ApplicationContextConstants.DEFAULT_PACKAGES);
        } else {
            packageNames = ApplicationContextConstants.DEFAULT_PACKAGES;
        }
        scanClass(packageNames);
    }

    /**
     * 注册Bean的注册信息，不包含依赖关系
     *
     * @param clazz 类对象
     */
    @Override
    public void registerBeanDefinitionWithoutDI(Class<?> clazz, InjectionProperty injectionProperty) {
        // 通过构造函数实例化Bean对象注册Bean信息
        BeanDefinition beanDefinition = new IocBeanDefinition();
        beanDefinition.setBeanClass(clazz);
        beanDefinition.setReturnType(clazz);
        String beanName = injectionProperty.getBeanName();
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
                methodBeanDefinition.setFactoryBeanName(beanName);
                methodBeanDefinition.setFactoryMethodName(method.getName());
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
                AopProxyFactories.getDefaultAopProxyFactory().addClassBeanName(beanName);
            }
            // 注册init方法和destroy方法
            registerInitAndDestroy(beanDefinition, method);
        }
    }

    /**
     * 添加Advisor增强器
     *
     * @param clazz    类对象
     */
    @Override
    public void addAdvisors(String beanName, Class<?> clazz) {
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
                AopProxyFactories.getDefaultAopProxyFactory().addClassBeanName(beanName);
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
                registerAdvisor(advisor);
                BeanDefinition beanDefinition = new IocBeanDefinition();
                beanDefinition.setFactoryBeanName(beanName);
                beanDefinition.setFactoryMethodName(method.getName());
                beanDefinition.setReturnType(returnType);
                registerBeanDefinition(adviceBeanName, beanDefinition);
                AopProxyFactories.getDefaultAopProxyFactory().addClassBeanName(beanName);
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
     * 注册Bean和方法的依赖关系
     *
     * @param clazz 类对象
     */
    private void registerBeanAndMethodDI(Class<?> clazz, BeanDefinition iocBeanDefinition) {
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
     * 获取该类的Bean名称和组件类型
     *
     * @param clazz 类对象
     * @return Bean名称
     */
    @Override
    public InjectionProperty getComponentPropertyValue(Class<?> clazz) {
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
        return new InjectionProperty(componentName, componentValue);
    }
}
