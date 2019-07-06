package com.lee.rokhan.container.context;

import com.lee.rokhan.common.utils.throwable.ThrowConsumer;
import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.pojo.ComponentProperty;
import com.lee.rokhan.container.pojo.InjectionProperty;
import com.lee.rokhan.container.processor.BeanPostProcessor;

import java.util.Set;


/**
 * Bean容器
 * 应用上下文
 * @author lichujun
 * @date 2019/6/26 10:25
 */
public interface ApplicationContext extends BeanFactory {

    /**
     * 处理扫描出的所有Class对象
     * @param consumer 处理
     * @throws Throwable 异常
     */
    void processScanClass(ThrowConsumer<Class<?>, Throwable> consumer) throws Throwable;

    /**
     * 处理所有组件的Class对象
     * @param consumer 处理
     * @throws Throwable 异常
     */
    void processAllComponentProperty(ThrowConsumer<ComponentProperty, Throwable> consumer) throws Throwable;

    /**
     * 处理指定组件的Class对象
     * @param consumer 处理
     * @param componentClass 组件的类对象
     * @throws Throwable 异常
     */
    void processComponentProperty(ThrowConsumer<ComponentProperty, Throwable> consumer, Class<?> componentClass) throws Throwable;

    /**
     * 添加Advisor增强器
     *
     * @param clazz    类对象
     */
    void addAdvisors(String beanName, Class<?> clazz);

    /**
     * 注册依赖关系
     * @param componentProperty 组件
     */
    void registerDIRelationship(ComponentProperty componentProperty);

    /**
     * 注册Bean的注册信息，不包含依赖关系
     *
     * @param clazz 类对象
     */
    void registerBeanDefinitionWithoutDI(Class<?> clazz, InjectionProperty injectionProperty);

    /**
     * 获取该类的Bean名称和组件类型
     *
     * @param clazz 类对象
     * @return Bean名称
     */
    InjectionProperty getComponentPropertyValue(Class<?> clazz);

    /**
     * 扫描Class
     * @param packageNames 包名集合
     * @throws Throwable 异常
     */
    void scanClass(Set<String> packageNames) throws Throwable;

    /**
     * 注册AOP增强器
     * @param advisor AOP增强器
     */
    void registerAdvisor(Advisor advisor);

    /**
     * 初始化扫描Class
     */
    void initScanClass() throws Throwable;

    /**
     * 通过接口类型获取实现的Bean对象的Bean名称
     * @param type 接口类型
     * @return Bean名称数组
     */
    Set<String> getBeanNamesByType(Class<?> type);

    /**
     * 注册监视Bean的生命周期的对象
     * @param beanPostProcessor 监视Bean的生命周期的对象
     */
    void registerBeanPostProcessor(BeanPostProcessor beanPostProcessor);
}
