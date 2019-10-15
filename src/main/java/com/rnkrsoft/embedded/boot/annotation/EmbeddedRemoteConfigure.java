package com.rnkrsoft.embedded.boot.annotation;

import com.rnkrsoft.framework.config.v1.RuntimeMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by rnkrsoft.com on 2019/5/24.
 * 嵌入式远程配置注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
public @interface EmbeddedRemoteConfigure {
    /**
     * 配置中心主机地址
     * @return 主机地址
     */
    String host() default "localhost";

    /**
     * 配置中心端口号
     * @return 端口号
     */
    int port() default 8080;

    /**
     * 组织编号
     * @return 组织编号
     */
    String groupId() default "";

    /**
     * 组件编号
     * @return 组件编号
     */
    String artifactId() default "";

    /**
     * 版本号
     * @return 版本号
     */
    String version() default "";

    /**
     * 环境名
     * @return 环境名
     */
    String env() default "PRO";

    /**
     * 运行模式
     * @return 运行模式
     */
    RuntimeMode runtimeMode() default RuntimeMode.LOCAL;

    /**
     * 安全密钥
     * @return 安全密钥
     */
    String securityKey() default "";
}