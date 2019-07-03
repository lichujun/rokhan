# rokhan

#####一、IoC容器：
######（1）定义BeanDefinition接口，用于描述Bean信息。实现BeanDefinition接口创建IocBeanDefinition类，用来存储Bean信息，方便实例化Bean对象和进行依赖注入。
######（2）定义BeanFactory接口，用于对Bean对象的创建和获取。实现BeanFactory接口创建IocBeanFactory类，用来管理所有Bean对象。当出现循环依赖的时候，使用二级缓存解决此问题，二级缓存里存放的是刚实例化的对象（没有进行依赖注入和初始化）。
######（3）BeanInstance接口，用于Bean对象的实例化（不做依赖注入）。有三种方式生成Bean对象：1.构造方法。2.静态方法。3.Bean对象的方法。
######（4）BeanReference类，用于存储Bean的名称，用于描述一个Bean。
######（5）PropertyValue类，存储Bean对象的成员变量的名称和需要注入的对象，可用于描述Bean对象的依赖。如果是BeanReference对象，则在BeanFactory创建和获取这个对象。

#####二、AOP方法增强
######（1）定义Advice接口，用于方法通知。定义了三种通知接口：1.前置通知：MethodBeforeAdvice。2.后置通知MethodReturnAdvice。3.环绕通知：MethodSurroundAdvice。
######（2）定义Pointcut接口，描述一个切点。实现一个支持AspectJ表达式的切点的类AspectJExpressionPointcut,通过AspectJ表达式匹配类和方法。
######（3）定义Advisor接口，可以理解成增强器，有三个重要成员变量：1.一个通知（Advice）的Bean名称。2.AspectJ表达式。3.切点。
######（4）定义BeanPostProcessor接口，用于Bean对象初始化之前或者之后所需要做的操作。AOP方法增强就是通过实现该接口，即AdvisorAutoProxyCreator类。
######（5）定义AopProxy接口，具体有两个实现，一个是JDK动态代理JdkDynamicAopProxy，一个是Cglib动态代理CglibDynamicAopProxy，通过动态代理对方法增强。如果进行依赖注入的时候，存在成员变量的类型是类而不是接口的话，就使用Cglib进行动态代理，除此之外使用JDK动态代理。