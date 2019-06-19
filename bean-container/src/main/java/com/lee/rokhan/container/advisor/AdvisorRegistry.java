package com.lee.rokhan.container.advisor;

import java.util.List;

/**
 * 提供对Advisor的注册和获取
 * @author lichujun
 * @date 2019/6/18 15:21
 */
public interface AdvisorRegistry {

    /**
     * 注册Advisor
     * @param advisor Advisor对象
     */
    void registerAdvisor(Advisor advisor);

    /**
     * 查询所有Advisor
     * @return 所有Advisor
     */
    List<Advisor> getAdvisors();
}
