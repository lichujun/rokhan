
import com.lee.rokhan.common.utils.ReflectionUtils;
import com.lee.rokhan.container.definition.impl.IocBeanDefinition;
import com.lee.rokhan.container.factory.BeanFactory;
import com.lee.rokhan.container.factory.impl.BeanFactories;
import com.lee.rokhan.container.pojo.BeanReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author lichujun
 * @date 2019/6/17 14:07
 */
public class Test {

    private static BeanFactory beanFactory = BeanFactories.getIocBeanFactory();

    public static void main(String[] args1) throws Throwable {
        /*IocBeanDefinition beanDefinition = new IocBeanDefinition();
        beanDefinition.setBeanClass(Bean1.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("destroy");

        beanFactory.registerBeanDefinition("bean1", beanDefinition);

        Bean1 obj = (Bean1) beanFactory.getBean("bean1");
        obj.doSomething();*/


        /*IocBeanDefinition beanDefinition1 = new IocBeanDefinition();
        beanDefinition1.setBeanClass(Bean1Factory.class);
        beanDefinition1.setFactoryMethodName("getBean1");
        beanDefinition1.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanDefinition1.setInitMethodName("init");
        beanDefinition1.setDestroyMethodName("destroy");

        beanFactory.registerBeanDefinition("bean1", beanDefinition1);
        Bean1 obj1 = (Bean1) beanFactory.getBean("bean1");
        obj1.doSomething();
        Bean1 obj2 = (Bean1) beanFactory.getBean("bean1");
        obj2.doSomething();*/

        IocBeanDefinition bd = new IocBeanDefinition();
        bd.setBeanClass(ABean.class);
        List<Object> args = new ArrayList<>();
        args.add("abean");
        args.add(new BeanReference("cbean"));
        bd.setArgumentValues(args);
        beanFactory.registerBeanDefinition("abean", bd);

        bd = new IocBeanDefinition();
        bd.setBeanClass(CBean.class);
        args = new ArrayList<>();
        args.add("cbean");
        bd.setArgumentValues(args);
        beanFactory.registerBeanDefinition("cbean", bd);

        //bf.preInstantiateSingletons();

        ABean abean = (ABean) beanFactory.getBean("abean");

        abean.doSomthing();

        /*CBean ccBean = new CCBean("hh");
        *//*Set<Field> fieldSet = ReflectionUtils.getDeclaredFields(ccBean);
        System.out.println(fieldSet);*//*
        Set<Method> methodSet = ReflectionUtils.getDeclaredMethods(ccBean);
        System.out.println(methodSet);*/
    }
}
