package com.rnkrsoft.embedded.boot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 嵌入式Boot应用
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface EmbeddedBootApplication {
    /**
     * 远程配置
     *
     * @return 远程配置注解
     */
    EmbeddedRemoteConfigure remoteConfigure() default @EmbeddedRemoteConfigure;

    /**
     * Servlet存放的根包路径
     * @return
     */
    String[] servletBasePackages() default {};
    /**
     * 执行的最大线程数
     *
     * @return 最大线程数
     */
    int maxThreads() default 100;

    /**
     * 异步执行超时时间
     *
     * @return 异步执行超时时间
     */
    int asyncTimeoutSecond() default 30 * 1000;

    /**
     * 连接超时时间
     *
     * @return 连接超时时间
     */
    int connectionTimeoutSecond() default 30 * 1000;

    /**
     * 运行时目录
     *
     * @return 运行时目录
     */
    String runtimeDir() default "./work";

    /**
     * 上下文路径
     *
     * @return 上下文路径
     */
    String contextPath() default "";

    /**
     * 协议
     *
     * @return 协议
     */
    String protocol() default "HTTP/1.1";

    /**
     * 文件编码
     *
     * @return 文件编码
     */
    String fileEncoding() default "UTF-8";

    /**
     * URI编码
     *
     * @return URI编码
     */
    String uriEncoding() default "UTF-8";

    /**
     * 查询参数是否编码
     * useBodyEncodingForURI的查询参数(QueryString)有效，他的设置对于URL和URI无效
     *
     * @return
     */
    boolean useBodyEncodingForURI() default true;

    /**
     * 主机名
     *
     * @return 主机名
     */
    String hostName() default "localhost";

    /**
     * 端口号
     *
     * @return 端口号
     */
    int port() default 8080;

    /**
     * 最大连接数
     *
     * @return 最大连接数
     */
    int maxConnections() default 30000;

    /**
     * 重新加载配置文件秒数
     *
     * @return 重新加载配置文件秒数
     */
    int reloadConfigSecond() default 60;
}
