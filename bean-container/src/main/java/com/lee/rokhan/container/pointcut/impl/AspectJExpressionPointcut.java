package com.lee.rokhan.container.pointcut.impl;

import com.lee.rokhan.container.pointcut.Pointcut;
import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.ShadowMatch;

import java.lang.reflect.Method;

/**
 * 使用AspectJ来匹配类或方法
 * @author lichujun
 * @date 2019/6/18 14:34
 */
public class AspectJExpressionPointcut implements Pointcut {

    // 定义全局的切点解析器
    private static final PointcutParser POINTCUT_PARSER = PointcutParser
            .getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution();

    /**
     * AspectJ表达式
     * 用来匹配类或者方法
     */
    private final String expression;
    private final PointcutExpression pointcutExpression;


    public AspectJExpressionPointcut(String expression) {
        super();
        this.expression = expression;
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

    public String getExpression() {
        return expression;
    }

}
