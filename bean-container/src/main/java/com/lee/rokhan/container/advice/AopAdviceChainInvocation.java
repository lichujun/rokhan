package com.lee.rokhan.container.advice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author lichujun
 * @date 2019/6/19 15:26
 */
public class AopAdviceChainInvocation {
    private static Method invokeMethod;
    static {
        try {
            invokeMethod = AopAdviceChainInvocation.class.getMethod("invoke");
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
    }

    private final Object proxy;
    private final Object target;
    private final Method method;
    private final Object[] args;
    private final List<Object> advices;

    public AopAdviceChainInvocation(Object proxy, Object target, Method method, Object[] args, List<Object> advices) {
        super();
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.advices = advices;
    }

    // 责任链执行记录索引号
    private int i = 0;

    public Object invoke() throws Throwable {
        if (i < this.advices.size()) {
            Object advice = this.advices.get(i++);
            if (advice instanceof MethodBeforeAdvice) {
                // 执行前置增强
                ((MethodBeforeAdvice) advice).before(method, args, target);
            } else if (advice instanceof MethodSurroudAdvice) {
                // 执行环绕增强和异常处理增强。注意这里给入的method 和 对象 是invoke方法和链对象
                return ((MethodSurroudAdvice) advice).invoke(invokeMethod, null, this);
            } else if (advice instanceof MethodReturnAdvice) {
                // 当是后置增强时，先得得到结果，再执行后置增强逻辑
                Object returnValue = this.invoke();
                ((MethodReturnAdvice) advice).afterReturn(returnValue, method, args, target);
                return returnValue;
            }
            return this.invoke();
        } else {
            return method.invoke(target, args);
        }
    }
}
