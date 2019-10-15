package com.rnkrsoft.embedded.httpserver;

import com.rnkrsoft.config.ConfigProvider;
import com.rnkrsoft.embedded.httpserver.server.ServletMetadata;
import com.rnkrsoft.embedded.httpserver.server.event.Event;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * HttpServer服务器，用于实现基于HTTP协议的容器
 */
public interface HttpServer extends LifeCycle{
    /**
     * 压入事件
     *
     * @param event 事件对象
     * @return
     */
    HttpServer publishEvent(Event event);

    /**
     * 绑定监听地址端口
     *
     * @param socketAddress 套接字地址对象
     * @param backlog       套接字处理队列长度
     * @throws IOException IO异常
     */
    HttpServer bind(InetSocketAddress socketAddress, int backlog) throws IOException;

    /**
     * 启动容器
     *
     * @param keepAliveMs 容器启动后保持服务的时间，如果为0则永不关闭，直到调用stop;否则keepAlive代表的毫秒时间到达后关闭容器
     */
    HttpServer start(int keepAliveMs);

    /**
     * 停止容器
     *
     * @param delayMs 延迟关闭时间
     */
    void stop(int delayMs);

    void await();

    ConfigProvider getConfig();

    /**
     * 根据映射url查找Servlet元信息
     * @param urlPattern url
     * @return 元信息
     */
    ServletMetadata lookupServletMetadata(String urlPattern);
    /**
     * 根据绑定的URL路径查询Servlet实例
     *
     * @param urlPattern URL路径
     * @param servletContext Servlet上下文
     * @return Servlet实例
     */
    Servlet lookupServlet(String urlPattern, ServletContext servletContext) throws ServletException;

    List<String> getWelcomes();
    /**
     * 以WebXml对象设置容器
     *
     * @param webXml WebXml对象,内容格式参照Servlet规范中的Web.xml
     */
    HttpServer setting(WebXml webXml);

    /**
     * 设置容器运行参数
     * @param name
     * @param value
     * @return
     */
    HttpServer setting(String name, String value);
    /**
     * 注册一个Servlet，并映射到URL上
     *
     * @param servletName   Servlet名称
     * @param servletClass  Servlet类对象
     * @param initParams    初始化参数
     * @param loadOnStartup 启动优先级
     *                      1)loadOnStartup标记容器是否应该在启动的时候加载这个servlet，(实例化并调用其init()方法)。
     *                      2)它的值必须是一个整数，表示servlet应该被载入的顺序
     *                      3)如果该元素不存在或者这个数为负时，则容器会当该Servlet被请求时，再加载。
     *                      4)当值为0或者大于0时，表示容器在应用启动时就加载并初始化这个servlet；
     *                      5)正数的值越小，该servlet的优先级越高，应用启动时就越先加载。
     *                      6)当值相同时，容器就会自己选择顺序来加载
     * @param urlPatterns    URL匹配地址
     */
    HttpServer register(String servletName, Class<? extends Servlet> servletClass, Properties initParams, int loadOnStartup, String ... urlPatterns);
}
