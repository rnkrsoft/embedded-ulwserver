package com.rnkrsoft.embedded.ulwserver;

import com.rnkrsoft.config.ConfigProvider;
import com.rnkrsoft.embedded.ulwserver.server.event.Event;

import javax.servlet.Servlet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Properties;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * Ultra Lightweight Server服务器，用于实现基于HTTP协议的容器
 */
public interface UlwServer extends LifeCycle{
    /**
     * 是否是调试版本
     */
    boolean DEBUG = true;
    /**
     * 压入事件
     *
     * @param event 事件对象
     * @return
     */
    UlwServer publishEvent(Event event);

    /**
     * 绑定监听地址端口
     *
     * @param socketAddress 套接字地址对象
     * @param backlog       套接字处理队列长度
     * @throws IOException IO异常
     */
    UlwServer bind(InetSocketAddress socketAddress, int backlog) throws IOException;
    HttpHandler[] getHandlers();
    /**
     * 启动容器
     *
     * @param keepAliveMs 容器启动后保持服务的时间，如果为0则永不关闭，直到调用stop;否则keepAlive代表的毫秒时间到达后关闭容器
     */
    UlwServer start(int keepAliveMs);

    /**
     * 停止容器
     *
     * @param delayMs 延迟关闭时间
     */
    void stop(int delayMs);

    /**
     * 容器主线程挂起等待结束
     */
    void await();

    /**
     * 处理事件
     */
    void handleEvent();
    /**
     * 获取配置
     * @return
     */
    ConfigProvider getConfig();

    /**
     * 注册的默认欢迎页面
     * @return
     */
    List<String> getWelcomes();

    /**
     * 设置容器运行参数
     * @param name
     * @param value
     * @return
     */
    UlwServer parameter(String name, String value);
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
    UlwServer register(String servletName, Class<? extends Servlet> servletClass, Properties initParams, int loadOnStartup, String ... urlPatterns);
}
