import com.lee.container.definition.BeanDefinition;
import com.lee.container.definition.impl.IocBeanDefinition;
import com.lee.container.factory.BeanFactory;
import com.lee.container.factory.impl.BeanFactories;

/**
 * @author lichujun
 * @date 2019/6/17 14:07
 */
public class Test {

    private static BeanFactory beanFactory = BeanFactories.getIocBeanFactory();

    public static void main(String[] args) throws Exception {
        /*IocBeanDefinition beanDefinition = new IocBeanDefinition();
        beanDefinition.setBeanClass(Bean1.class);
        beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinition.setInitMethodName("init");
        beanDefinition.setDestroyMethodName("destroy");

        beanFactory.registerBeanDefinition("bean1", beanDefinition);

        Bean1 obj = (Bean1) beanFactory.getBeanByName("bean1");
        obj.doSomething();*/


        IocBeanDefinition beanDefinition1 = new IocBeanDefinition();
        beanDefinition1.setBeanClass(Bean1Factory.class);
        beanDefinition1.setFactoryMethodName("getBean1");
        beanDefinition1.setScope(BeanDefinition.SCOPE_PROTOTYPE);
        beanDefinition1.setInitMethodName("init");
        beanDefinition1.setDestroyMethodName("destroy");

        beanFactory.registerBeanDefinition("bean1", beanDefinition1);
        Bean1 obj1 = (Bean1) beanFactory.getBeanByName("bean1");
        obj1.doSomething();
        Bean1 obj2 = (Bean1) beanFactory.getBeanByName("bean1");
        obj2.doSomething();
    }
}
