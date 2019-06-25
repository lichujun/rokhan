package com.lee.rokhan.container;

import com.lee.rokhan.container.annotation.*;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * Ioc容器初始化
 * @author lichujun
 * @date 2019/6/25 16:30
 */
@AllArgsConstructor
public class IocApplicationContext {

    private final Set<Class<?>> classSet;

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
            if (StringUtils.isBlank(beanName)) {
                continue;
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
