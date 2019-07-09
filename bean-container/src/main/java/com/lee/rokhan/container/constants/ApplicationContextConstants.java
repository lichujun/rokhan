package com.lee.rokhan.container.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * 应用上下文的常量
 * @author lichujun
 * @date 2019/6/28 15:26
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationContextConstants {

    public static final String SCAN_PACKAGES = "scanPackages";

    public static final String ACTIVE = "active";

    public static final String ENVIRONMENT = "environment";

    public static final String BRACKETS = "()";

    public static final Set<String> DEFAULT_PACKAGES = new HashSet<String>() {{
        add("com.lee.rokhan.vertx.web");
        add("com.lee.rokhan.container");
    }};

}
