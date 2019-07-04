
# ***rokhan***
##### 一、***IoC***容器：
###### （1）定义***BeanDefinition***接口，用于描述Bean信息。实现***BeanDefinition***接口创建***IocBeanDefinition***类，用来存储***Bean***信息，方便实例化***Bean***对象和进行依赖注入。所有Bean对象的生成都是通过BeanDefinition创建。***Bean***的定义信息有如下的属性：
~~~
    /**
     * Bean的类对象
     */
    private Class<?> beanClass;

    /**
     * scope类型
     */
    private String scope = BeanDefinition.SCOPE_SINGLETON;

    /**
     * 工厂Bean名称，用于通过该Bean对象的方法或静态方法实例化对象
     */
    private String factoryBeanName;

    /**
     * 工厂方法名称，用于通过方法或静态方法实例化对象
     */
    private String factoryMethodName;

    /**
     * 初始化方法
     */
    private String initMethodName;

    /**
     * 销毁方法
     */
    private String destroyMethodName;

    /**
     * 构造方法、静态方法或Bean对象方法的参数对象列表
     */
    private List<Object> argumentValues;

    /**
     * 构造函数
     * 注：用于缓存，用于生成prototype类型的对象
     */
    private Constructor<?> constructor;

    /**
     * 依赖
     */
    private List<PropertyValue> propertyValues;

    /**
     * 工厂方法
     * 注：用于缓存，用于生成prototype类型的对象
     */
    private Method factoryMethod;
~~~
###### （2）定义***BeanFactory***接口，用于对***Bean***对象的创建和获取。实现***BeanFactory***接口创建***IocBeanFactory***类，用来管理所有***Bean***对象。当出现循环依赖的时候，使用二级缓存解决此问题，二级缓存里存放的是刚实例化的对象（没有进行依赖注入和初始化）。
- 定义二级缓存容器：
~~~
// 存放Bean对象的容器，一级缓存
private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(DEFAULT_SIZE);

// Bean对象的二级缓存
private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(DEFAULT_SIZE);
~~~
- 在依赖注入获取***Bean***对象的过程中，先在***Bean***对象容器（一级缓存）查找***Bean***对象，如果没有则去二级缓存查找***Bean***对象。当***Bean***对象实例化后将该对象放入到二级缓存中：
~~~
    private Object doGetBean(String beanName) throws Throwable {
        // 先从Bean对象容器里去取值，如果获取为空，则创建对象
        Object beanObject = singletonObjects.get(beanName);
        if (beanObject != null) {
            return beanObject;
        }
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (earlySingletonObjects.keySet().contains(beanName)) {
            beanObject = earlySingletonObjects.get(beanName);
        } else {
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
            // 初始化对象之前处理
            beanObject = applyPostProcessBeforeInitialization(beanObject, beanName);
            // 对象初始化
            doInit(beanObject, beanDefinition);
            // 初始化对象之后的处理
            beanObject = applyPostProcessAfterInitialization(beanObject, beanName);
        }

        // 如果是单例模式，则缓存到Map容器
        if (beanDefinition.isSingleton()) {
            singletonObjects.put(beanName, beanObject);
        }
        return beanObject;
    }
~~~
###### （3）***BeanInstance***接口，用于***Bean***对象的实例化（不做依赖注入）。有三种方式生成***Bean***对象：1.构造方法。2.静态方法。3.***Bean***对象的方法。通过***Bean***的定义信息和***Bean***工厂实例化对象。
- 通过构造函数实例化对象
~~~
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Throwable {
        try {
            Object[] args = beanDefinition.getArgumentRealValues(beanFactory);
            if (args == null) {
                return beanDefinition.getBeanClass().newInstance();
            } else {
                Constructor constructor = determineConstructor(beanDefinition, args);
                constructor.setAccessible(true);
                return constructor.newInstance(args);
            }
        } catch (SecurityException e) {
            log.error("创建bean的实例异常,beanDefinition" + beanDefinition, e);
            throw e;
        }
    }
~~~
- 通过静态方法实例化对象
~~~
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Throwable {
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object[] args = beanDefinition.getArgumentRealValues(beanFactory);
        Method method = determineMethod(beanDefinition, args, beanClass);
        method.setAccessible(true);
        return method.invoke(beanClass, args);
    }
~~~
- 通过***Bean***对象的方法实例化对象
~~~
    @Override
    public Object instance(BeanDefinition beanDefinition, BeanFactory beanFactory) throws Throwable {
        // 校验参数
        Objects.requireNonNull(beanDefinition.getFactoryBeanName(), "工厂Bean名称factoryBeanName不能为空");
        Objects.requireNonNull(beanDefinition.getFactoryMethodName(), "工厂方法名称factoryMethodName不能为空");

        String factoryBeanName = beanDefinition.getFactoryBeanName();
        Object factoryBean = beanFactory.getBean(factoryBeanName);
        Objects.requireNonNull(factoryBean, "工厂Bean：factoryBean不能为空");

        Object[] args = beanDefinition.getArgumentRealValues(beanFactory);
        Class<?> factoryBeanClass = factoryBean.getClass();
        Method method = determineMethod(beanDefinition, args, factoryBeanClass);
        method.setAccessible(true);
        return method.invoke(factoryBean, args);
    }
~~~
###### （4）***BeanReference***类，用于存储***Bean***的名称，用于描述一个***Bean***。
~~~
    @Getter
    @AllArgsConstructor
    public class BeanReference {

        /**
         * Bean名称
         */
        private final String beanName;
    }
~~~
###### （5）***PropertyValue***类，存储***Bean***对象的成员变量的名称和需要注入的对象，可用于描述***Bean***对象的依赖。如果是***BeanReference***对象，则在***BeanFactory***创建和获取这个对象。
~~~
    @Getter
    @AllArgsConstructor
    public class PropertyValue {
        /**
         * Bean名称
         */
        private final String name;

        /**
         * Bean对象
         */
        private final Object value;
    }
~~~
###### （6）***Aware***注入外界属性，即***Bean***名称、***Bean***工厂和应用上下文。
- ***Aware***接口
~~~
    /**
     * 注册Bean对象时，注入对应属性，让Bean对象获取到外界信息
     * @author lichujun
     * @date 2019/7/4 11:02
     */
    public interface Aware {
    }
~~~
- ***BeanNameAware***接口
~~~
    /**
     * 让Bean对象获取到Bean名称
     * @author lichujun
     * @date 2019/7/4 11:03
     */
    public interface BeanNameAware extends Aware {

        /**
         * 设置Bean名称
         * @param beanName Bean名称
         */
        void setBeanName(String beanName);
    }
~~~
- ***BeanFactory***接口
~~~
    /**
     * 让Bean对象获取到Bean工厂
     * @author lichujun
     * @date 2019/7/4 11:04
     */
    public interface BeanFactoryAware extends Aware {

        /**
         * 设置Bean工厂
         * @param beanFactory Bean工厂
         */
        void setBeanFactory(BeanFactory beanFactory) throws Throwable;
    }
~~~
- ***ApplicationContextAware***接口
~~~
    /**
     * 让Bean对象获取到应用上下文
     * @author lichujun
     * @date 2019/7/4 11:05
     */
    public interface ApplicationContextAware extends Aware {

        /**
         * 设置应用上下文
         * @param applicationContext  应用上下文
         */
        void setApplicationContext(ApplicationContext applicationContext);
    }
~~~
- ***Bean***对象实例化并依赖注入后调用***set***方法注入外界属性
~~~
    if (BeanNameAware.class.isAssignableFrom(beanObjectClass)) {
        ((BeanNameAware) beanObject).setBeanName(beanName);
    }
    if (BeanFactoryAware.class.isAssignableFrom(beanObjectClass)) {
        ((BeanFactoryAware) beanObject).setBeanFactory(this);
    }
    if (ApplicationContextAware.class.isAssignableFrom(beanObjectClass)) {
        ((ApplicationContextAware) beanObject).setApplicationContext((ApplicationContext) this);
    }
~~~
##### 二、***AOP***方法增强
###### （1）定义***Advice***接口，用于方法通知。定义了三种通知接口：1.前置通知：***MethodBeforeAdvice***。2.后置通知***MethodReturnAdvice***。3.环绕通知：***MethodSurroundAdvice***。
- 前置通知接口
~~~
    public interface MethodBeforeAdvice extends Advice {

        /**
         * 实现方法的前置增强
         * @param method 方法
         * @param args 方法的参数列表对象
         * @param target 方法的目标对象
         */
        void before(Method method, Object[] args, Object target) throws Throwable;
    }
~~~
- 后置通知接口
~~~
    public interface MethodReturnAdvice extends Advice {

        /**
         * 方法的后置增强
         * @param returnValue 方法执行后的返回值
         * @param method 方法
         * @param args 方法的参数列表对象
         * @param target 方法的目标对象
         */
        void afterReturn(Object returnValue, Method method, Object[] args, Object target) throws Throwable;
    }
~~~
- 环绕通知接口
~~~
    public interface MethodSurroundAdvice extends Advice {

        /**
         * 方法的环绕通知
         * @param method 方法
         * @param args 方法的参数列表对象
         * @param target 方法的目标对象
         * @return 方法执行后的返回值
         */
        Object invoke(Method method, Object[] args, Object target) throws Throwable;
    }
~~~
###### （2）定义***Pointcut***接口，描述一个切点。实现一个支持***AspectJ***表达式的切点的类***AspectJExpressionPointcut***,通过***AspectJ***表达式匹配类和方法。
- ***Pointcut***接口
~~~
    public interface Pointcut {

        /**
         * 匹配类
         * @param targetClass 目标类对象
         * @return 是否匹配成功
         */
        boolean matchClass(Class<?> targetClass);

        /**
         * 匹配方法
         * @param targetMethod 目标方法
         * @return 是否匹配成功
         */
        boolean matchMethod(Method targetMethod);
    }
~~~
- ***AspectJ***表达式实现***Advisor***增强器
~~~
    public class AspectJExpressionPointcut implements Pointcut {

        // 定义全局的切点解析器
        private static final PointcutParser POINTCUT_PARSER = PointcutParser
                .getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();

        /**
         * AspectJ表达式
         * 用来匹配类或者方法
         */
        private final PointcutExpression pointcutExpression;

        public AspectJExpressionPointcut(String expression) {
            this.pointcutExpression = POINTCUT_PARSER.parsePointcutExpression(expression);
        }

        @Override
        public boolean matchClass(Class<?> targetClass) {
            return pointcutExpression.couldMatchJoinPointsInType(targetClass);
        }

        @Override
        public boolean matchMethod(Method targetMethod) {
            ShadowMatch sm = pointcutExpression.matchesMethodExecution(targetMethod);
            return sm.alwaysMatches();
        }

    }
~~~
###### （3）定义***Advisor***接口，可以理解成增强器，有三个重要成员变量：1.一个通知（***Advice***）的***Bean***名称。2.***AspectJ***表达式。3.切点。
~~~
    @Getter
    public class AspectJPointcutAdvisor implements Advisor {

        private final String adviceBeanName;

        private final String expression;

        private final Pointcut pointcut;

        public AspectJPointcutAdvisor(String adviceBeanName, String expression) {
            super();
            this.adviceBeanName = adviceBeanName;
            this.expression = expression;
            this.pointcut = new AspectJExpressionPointcut(expression);
        }
    }
~~~
###### （4）定义***BeanPostProcessor***接口，用于***Bean***对象初始化之前或者之后所需要做的操作。***AOP***方法增强就是通过实现该接口，即***AdvisorAutoProxyCreator***类。
- ***Bean***对象初始化后做方法增强
~~~
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws Throwable {
        // 在此判断bean是否需要进行切面增强
        List<Advisor> matchAdvisors = getMatchedAdvisors(bean, beanName);
        // 如需要就进行增强,再返回增强的对象。
        if (CollectionUtils.isNotEmpty(matchAdvisors)) {
            bean = this.createProxy(bean, beanName, matchAdvisors);
        }
        return bean;
    }
~~~
- 获取匹配到所有的增强器
~~~
    private List<Advisor> getMatchedAdvisors(Object bean, String beanName) {
        if (CollectionUtils.isEmpty(advisors)) {
            return null;
        }

        // 得到类、所有的方法
        Class<?> beanClass = bean.getClass();
        Set<Method> allMethods = ReflectionUtils.getDeclaredMethods(beanClass);

        // 存放匹配的Advisor的list
        List<Advisor> matchAdvisors = new ArrayList<>();
        // 遍历Advisor来找匹配的
        for (Advisor ad : advisors) {
            if (ad instanceof AspectJPointcutAdvisor) {
                if (isPointcutMatchBean(ad, beanClass, allMethods)) {
                    matchAdvisors.add(ad);
                }
            }
        }
        return matchAdvisors;
    }
~~~
- 动态代理进行方法增强
~~~
private Object createProxy(Object bean, String beanName, List<Advisor> matchAdvisors) throws Throwable {
        // 通过AopProxyFactory工厂去完成选择、和创建代理对象的工作。
        return AopProxyFactories.getDefaultAopProxyFactory()
                .createAopProxy(bean, beanName, matchAdvisors, beanFactory)
                .getProxy();
    }
~~~
###### （5）定义***AopProxy***接口，具体有两个实现，一个是***JDK***动态代理***JdkDynamicAopProxy***，一个是***Cglib***动态代理***CglibDynamicAopProxy***，通过动态代理对方法增强。再定义一个动态代理工厂类***DefaultAopProxyFactory***，如果进行依赖注入的时候，存在成员变量的类型是类而不是接口的话，就使用Cglib进行动态代理，除此之外使用***JDK***动态代理。
- ***JDK***动态代理
~~~
    @Slf4j
    @AllArgsConstructor
    public class JdkDynamicAopProxy implements AopProxy, InvocationHandler {

        /**
         * Bean名称
         */
        private final String beanName;
        /**
         * Bean对象
         */
        private final Object target;
        /**
         * 匹配到的增强器
         */
        private final List<Advisor> matchAdvisors;
        /**
         * Bean工厂
         */
        private final BeanFactory beanFactory;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return AopProxyUtils.applyAdvices(target, method, args, matchAdvisors, proxy, beanFactory);
        }

        @Override
        public Object getProxy() {
            return this.getProxy(target.getClass().getClassLoader());
        }

        @Override
        public Object getProxy(ClassLoader classLoader) {
            if (log.isDebugEnabled()) {
                log.debug("为" + target + "创建代理。");
            }
            return Proxy.newProxyInstance(classLoader, target.getClass().getInterfaces(), this);
        }
    }
~~~
- ***Cglib***动态代理
~~~
    @Slf4j
    @AllArgsConstructor
    public class CglibDynamicAopProxy implements AopProxy, MethodInterceptor {

        private static final Enhancer ENHANCER = new Enhancer();

        /**
         * Bean名称
         */
        private final String beanName;
        /**
         * Bean对象
         */
        private final Object target;
        /**
         * 匹配到的增强器
         */
        private final List<Advisor> matchAdvisors;
        /**
         * Bean工厂
         */
        private final BeanFactory beanFactory;

        @Override
        public Object getProxy() throws Throwable {
            return this.getProxy(target.getClass().getClassLoader());
        }

        @Override
        public Object getProxy(ClassLoader classLoader) throws Throwable {
            if (log.isDebugEnabled()) {
                log.debug("为" + target + "创建cglib代理。");
            }
            Class<?> superClass = this.target.getClass();
            ENHANCER.setSuperclass(superClass);
            ENHANCER.setInterfaces(this.getClass().getInterfaces());
            ENHANCER.setCallback(this);
            Constructor<?> constructor = null;
            try {
                constructor = superClass.getDeclaredConstructor();
            } catch (NoSuchMethodException | SecurityException e) {
                log.warn("获取构造方法发生异常");
            }
            if (constructor != null) {
                return ENHANCER.create();
            } else {
                BeanDefinition bd = beanFactory.getBeanDefinition(beanName);
                return ENHANCER.create(bd.getConstructor().getParameterTypes(), bd.getArgumentRealValues(beanFactory));
            }
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            return AopProxyUtils.applyAdvices(target, method, args, matchAdvisors, proxy, beanFactory);
        }

    }
 ~~~
 - 动态代理工厂类，如果有成员变量的类型是类而不是接口的时候，调用***addClassBeanName***方法，选择合适的动态代理
 ~~~
     public class DefaultAopProxyFactory implements AopProxyFactory {

        /**
         * 通过类去匹配Bean对象
         */
        private final Set<String> classBeanNames = new HashSet<>();

        @Override
        public AopProxy createAopProxy(Object bean, String beanName, List<Advisor> matchAdvisors, BeanFactory beanFactory) {
            if (shouldUseJDKDynamicProxy(bean, beanName)) {
                return new JdkDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
            } else {
                return new CglibDynamicAopProxy(beanName, bean, matchAdvisors, beanFactory);
            }
        }

        private boolean shouldUseJDKDynamicProxy(Object bean, String beanName) {
            return !classBeanNames.contains(beanName);
        }

        publ vo addClasString classBeanName) {
            classBeanNames.add(classBeanName);
        }
    }
~~~
