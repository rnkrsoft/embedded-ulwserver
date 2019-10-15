# 嵌入式HttpServer容器
本项目用于实现FAT JAR方式的项目部署，对传统WAR项目进行一定的重构可以打包成一个JAR，方便的进行启动和部署。https://github.com/rnkrsoft/embedded-tomcat

最新版本:[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.rnkrsoft.embedded/embedded-httpserver/badge.svg)](http://search.maven.org/#search|ga|1|g%3A%22com.rnkrsoft.embedded%22%20AND%20a%3A%22embedded-httpserver%22)

```xml
<dependency>
    <groupId>com.rnkrsoft.embedded</groupId>
    <artifactId>embedded-httpserver</artifactId>
    <version>最新版本</version>
</dependency>
```

项目中的需要在project/build/plugins/增加plugin节点用于将当前jar包打包成FAT JAR。

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>1.3.3.RELEASE</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```



## 1 启动容器

引用以上Maven坐标在pomxml中，并在src/main/java/下任意包下新建任意类名的含有main(String[] args)入口方法

```java
import com.rnkrsoft.embedded.httpserver.HttpServerStartup;

public static void  main(String[] args){
	HttpServerStartup.main();
}
```

可以启动一个默认监听8080的端口的容器。

## 2 特性

## 2.1 Boot方式启动容器

类似Spring Boot方式启动Tomcat容器。最简单的启动代码如下代码：

```java
package com.rnkrsoft.embedded.demo;

import com.rnkrsoft.embedded.boot.annotation.EmbeddedBootApplication;
import com.rnkrsoft.embedded.boot.annotation.EmbeddedRemoteConfigure;
import com.rnkrsoft.framework.config.v1.RuntimeMode;
import org.junit.Test;

@EmbeddedBootApplication
public class DemoMain {

	public static void  main(String[] args){
        EmbeddedApplicationLoader.runWith(DemoMain.class, args);
    }
}
```

### 2.1.1 @EmbeddedBootApplication注解

用于标注在启动类上，指定应用容器的启动参数，注解上有各种可以定制容器的参数。

| 参数名                  | 默认值                   | 说明                                                         |
| ----------------------- | ------------------------ | ------------------------------------------------------------ |
| remoteConfigure         | @EmbeddedRemoteConfigure | 用于进行远程配置，默认处于本地模式                           |
| maxThreads              | 100                      | 最大线程数，大并发请求时，tomcat能创建来处理请求的最大线程数，超过则放入请求队列中进行排队，默认值为100 |
| asyncTimeoutSecond      | 30 * 1000                | 以毫秒为单位的异步请求超时时间。若是没有指定，该属性被设置为30000（30秒）。 |
| connectionTimeoutSecond | 30 * 1000                | 网络连接超时，假设设置为0表示永不超时，这样设置隐患巨大，通常可设置为30000ms，默认30000ms |
| runtimeDir              | ./work                   | 运行时目录                                                   |
| contextPath             | 空字符串                 | 上下文路径                                                   |
| protocol                | HTTP/1.1                 | 协议                                                         |
| fileEncoding            | UTF-8                    | 系统默认文件编码                                             |
| uriEncoding             | UTF-8                    | GET方式提交数据时采用UTF8编码                                |
| useBodyEncodingForURI   | true                     | 查询参数是否编码                                             |
| hostName                | localhost                | 主机名                                                       |
| port                    | 8080                     | 端口号                                                       |
| maxConnections          | 30000                    | 最大连接数，表示有多少个socket连接到tomcat上                 |
| reloadConfigSecond      | 60                       | 重新加载配置文件tomcat.properties秒数                        |




### 2.1.2 EmbeddedApplicationLoader
EmbeddedApplicationLoader为加载器，传入标注有@EmbeddedBootApplication注解的类对象和入参数组。

### 2.1.3 @EmbeddedRemoteConfigure

该注解用于配置容器远程配置运行模式。embedded-tomcat支持氡氪网络科技的OpenConfigure（https://github.com/rnkrsoft/OpenConfigure）配置中心。配置中心客户端已经集成在embedded-tomcat中，可将容器运行的参数进行中心化控制。当运行模式配置为REMOTE时，启动容器时先连接配置中心获取groupId:artifactId:version坐标对应的配置参数。


| 参数名      | 默认值         | 说明 |
| ----------- | -------------- | ---- |
| host        | localhost | 配置中心地址 |
| port        | 8080 | 配置中心端口号 |
| groupId     |                | 组织编号 |
| artifactId  |           | 组件编号 |
| version     |           | 版本号 |
| env         | PRO | 环境名 |
| runtimeMode | LOCAL | 运行模式 |
| securityKey |  | 安全密钥 |

### 2.2 Servlet支持

Servlet（Server Applet）是Java Servlet的简称，支持Servlet2.0和Servlet3.0。
1. Servlet2.0
在开发过程只需遵守Servlet规范开发即可。需要在src/main/resource/WEB-INF/web.xml中进行Servlet配置，具体配置参照Servlet2.0开发规范。
2. Servlet3.0
在开发过程只需遵守Servlet规范,在Servlet上标注注解@WebServlet,参见如下例子：
```java
package com.demo.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.web.skeleton4j.config.Skeleton4jConfig;
import javax.web.skeleton4j.service.Skeleton4jService;
import javax.web.skeleton4j.servlet.ServletConstant;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/2/14.
 * name="demo"是指定Servelt名称，保证全局唯一
 * urlPatterns = "/demo"是访问地址，可以使用http://localhost:端口号/demo访问
 */
@WebServlet(name = "demo", urlPatterns = "/demo")
public class DemoServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        //TODO 此处编写逻辑
    }
}

```
### 2.3 JSP支持
JSP（全称JavaServer Pages）是一种动态网页技术标准。JSP将Java代码和特定变动内容嵌入到静态的页面中，实现以静态页面为模板，动态生成其中的部分内容。
JSP在开发时依然遵循JSP规范，可以使用EL等标签语言。JSP必须存放在/src/main/resources/META-INF/下。如果使用Spring MVC则可以放在/src/main/resources/WEB-INF/下。

### 2.4 Spring MVC集成
Spring MVC是Spring 的MVC框架，支持以@Controller的Web控制器进行地址暴露。支持JSP视图或者其他模板视图技术。集成Spring MVC时需要在原有的web.xml添加Spring MVC配置。参见如下参考配置：
web.xml文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd"
	version="3.0">
	<display-name>xxx系统名称</display-name>
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<!--此处配置Spring容器的上下文配置，注意此处要以classpath*:开头，说明加载所有的applicationContext.xml-->
		<param-value>classpath*:applicationContext.xml</param-value>
	</context-param>

	<!--Spring ApplicationContext 载入 -->
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<filter>
		<filter-name>encodingFilter</filter-name>
		<filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>UTF-8</param-value>
		</init-param>
		<init-param>
			<param-name>forceEncoding</param-name>
			<param-value>true</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>encodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>


	<welcome-file-list>
		<welcome-file>index.jsp</welcome-file>
	</welcome-file-list>

	<!--通过Spring MVC暴露的服务-->
	<servlet>
		<servlet-name>springMVC</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<!--在src/main/resource/下新建spring-mvc.xml，具体内容参照下面的spring-mvc.xml文件-->
			<param-value>classpath*:spring-mvc.xml</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>springMVC</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>
</web-app>
```

spring-mvc.xml文件
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd


		http://www.springframework.org/schema/beans
		http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context
		http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- 自动扫描且只扫描@Controller com.xxx为具体项目包名 -->
    <context:component-scan base-package="com.xxx.**.web.controller" use-default-filters="false">
        <context:include-filter type="annotation" expression="org.springframework.stereotype.Controller" />
        <context:include-filter type="annotation" expression="org.springframework.web.bind.annotation.ControllerAdvice" />
    </context:component-scan>

    <!-- 当返回值为字符串时, 为了避免乱码问题, 将默认字符集设置为UTF-8(默认是ISO-8859-1) -->
    <mvc:annotation-driven>
        <mvc:message-converters register-defaults="true">
            <bean class="org.springframework.http.converter.StringHttpMessageConverter" c:defaultCharset="UTF-8" />
        </mvc:message-converters>
    </mvc:annotation-driven>

    <!-- 定义JSP文件的位置 -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver"
          p:viewClass="org.springframework.web.servlet.view.JstlView"
          p:prefix="/WEB-INF/views/"
          p:suffix=".jsp" />

    <!-- 容器默认的DefaultServletHandler处理 所有静态内容与无RequestMapping处理的URL -->
    <mvc:default-servlet-handler />
</beans>
```
applicationContext.xml文件
```xml
<?xml version="1.0" encoding="utf-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd


        http://www.springframework.org/schema/aop
        http://www.springframework.org/schema/aop/spring-aop.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
    <!--加载所有JAR包和src/main/resources/下的applicationContext-开头的Spring配置文件-->
    <import resource="classpath*:applicationContext-**.xml"/>
</beans>
```
## 3 项目部署

通常我们在项目中执行mvn install后打包成jar后可以使用embedded-tomcat内部集成的cli进行生成部署脚本。

执行如下命令生成部署脚本:

```
java -jar boot.jar -s
```

在当前目录下生成如下文件：

```
log4j2.xml
startup.bat
startup.sh
```

windows下双击或者运行startup.bat，linux或者mac下双击或者运行startup.sh。

## 3.1 日志

默认情况下embedded-tomcat带有的日志配置文件是：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration status="WARN" monitorInterval="30">
    <Properties>
        <Property name="FILE_SIZE">300M</Property>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</Property>
        <Property name="ROLLING_FILE_NAME">./logs/app.log</Property>
        <Property name="ROLLOVER_STRATEGY_MAX">120</Property>
    </Properties>
    <appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>
        <RollingRandomAccessFile name="RollingFile" fileName="${ROLLING_FILE_NAME}" filePattern="${ROLLING_FILE_NAME}.%d{yyyy-MM-dd}.%i" immediateFlush="false">
            <PatternLayout pattern="${LOG_PATTERN}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
            </Policies>
            <DefaultRolloverStrategy max="${ROLLOVER_STRATEGY_MAX}"/>
        </RollingRandomAccessFile>
    </appenders>

    <loggers>
        <AsyncRoot level="debug">
            <appender-ref ref="RollingFile"/>
        </AsyncRoot>
         <!--
            additivity开启的话，由于这个logger也是满足root的，所以会被打印两遍。
            -->
        <logger name="org.springframework" level="info" additivity="false">
            <appender-ref ref="RollingFile"/>
        </logger>
        <logger name="com.rnkrsoft.framework" level="debug" additivity="false">
            <appender-ref ref="RollingFile"/>
        </logger>
        <logger name="com.rnkrsoft.embedded" level="debug" additivity="false">
            <appender-ref ref="RollingFile"/>
        </logger>
    </loggers>
</configuration>
```

默认输入日志为滚动日志。

### 3.2 标准输出日志

如果需要设置为标准输出，则需要将ref="RollingFile"替换为ref="Console"，日志将以标准输出在控制台。

### 3.3 滚动日志文件输出日志

如果需要设置为滚动日志文件输出日志，则需要将ref="Console"替换为ref="RollingFile"，日志将以日志文件方式输出。

## 4 命令行
### 4.1 -generate(-g)
生成容器运行的配置文件tomcat.properties，该文件位于当前jar包运行的work目录下。
### 4.2 -script(-s)
生成容器运行的部署脚本log4j2.xml，startup.sh,startup.bat文件，用于实现启动日志的外部配置，JVM参数设置等定制化。
### 4.3 -pom(-p)
生成开发的maven项目脚手架目录，已将需要的pom.xml配置配置就绪。