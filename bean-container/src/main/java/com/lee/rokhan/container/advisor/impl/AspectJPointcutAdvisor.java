package com.lee.rokhan.container.advisor.impl;

import com.lee.rokhan.container.advisor.Advisor;
import com.lee.rokhan.container.pointcut.Pointcut;
import com.lee.rokhan.container.pointcut.impl.AspectJExpressionPointcut;
import lombok.Getter;

/**
 * AspectJ增强器
 * @author lichujun
 * @date 2019/6/18 15:06
 */
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
