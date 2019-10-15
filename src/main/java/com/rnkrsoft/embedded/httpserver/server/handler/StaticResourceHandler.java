package com.rnkrsoft.embedded.httpserver.server.handler;


import com.rnkrsoft.embedded.httpserver.HttpConnection;
import com.rnkrsoft.embedded.httpserver.HttpProtocol;
import com.rnkrsoft.embedded.httpserver.HttpServer;
import com.rnkrsoft.embedded.httpserver.server.HttpHeader;
import com.rnkrsoft.embedded.httpserver.server.io.IOUtils;
import com.rnkrsoft.embedded.httpserver.server.mime.MimeRegistry;
import com.rnkrsoft.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 * 静态资源处理器
 */
@Slf4j
public class StaticResourceHandler extends AbstractHandler {

    public StaticResourceHandler(HttpServer server) {
        super(server);
    }

    @Override
    public boolean handleRequest(HttpConnection connection) throws IOException {
        return loadStaticResource(connection);
    }

    /**
     * 加载静态资源
     * @param connection 连接对象
     * @return 是否已处理
     * @throws IOException IO异常
     */
    boolean loadStaticResource(HttpConnection connection) throws IOException {
        URI uri = connection.getUri();
        if (log.isDebugEnabled()) {
            log.debug("begin load static resource '{}'", uri.getPath());
        }
        //获取当前连接绑定的协议
        HttpProtocol protocol = connection.getProtocol();
        //应答头信息
        HttpHeader responseHeader = connection.getResponseHeader();
        OutputStream rawOut = connection.getRawOut();
        //路径
        String path = "";
        //文件名
        String fileName = uri.getPath();
        //如果是首页，则遍历设置的，看是否能够加载成功
        if (fileName.equals("/")) {
            List<String> welcomes = connection.getServer().getWelcomes();
            for (String welcome : welcomes) {
                if (loadStaticResource(connection, protocol, responseHeader, rawOut, path, welcome)) {
                    log.debug("load welcome page '{}' is successful.", welcome);
                    return true;
                }
                if (log.isDebugEnabled()) {
                    log.debug("load welcome page '{}' is failure.", welcome);
                }
            }
            return false;
        } else {
            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }
            int lastPos = fileName.lastIndexOf("/");
            if (lastPos > -1) {
                path = fileName.substring(0, lastPos);
                fileName = fileName.substring(lastPos + 1);
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
            }
            return loadStaticResource(connection, protocol, responseHeader, rawOut, path, fileName);
        }

    }

    /**
     * 加载静态资源
     * @param connection 连接对象
     * @param protocol 协议对象
     * @param responseHeader 应答头信息
     * @param rawOut 原生输出流
     * @param path 路径
     * @param fileName 文件名
     * @return 是否处理
     */
    boolean loadStaticResource(HttpConnection connection, HttpProtocol protocol, HttpHeader responseHeader, OutputStream rawOut, String path, String fileName) {
        if (log.isDebugEnabled()) {
            log.debug("try load welcome page '{}'...", fileName);
        }
        InputStream is;
        try {
            int lastFilePos = fileName.lastIndexOf(".");
            String extension = "";
            //文件资源必须有后缀名
            if (lastFilePos > -1) {
                extension = fileName.substring(lastFilePos + 1);
                extension = extension.toLowerCase();
            }
            String name = connection.getServer().getConfig().getString("WEB_ROOT", "META-INF/resources");
            if (name.charAt(name.length() - 1) != '/'){
                name += "/";
            }
            if (path.isEmpty()) {
                name += fileName;
            } else {
                name += path + "/" + fileName;
            }
            URL url = this.getClass().getClassLoader().getResource(name);
            if (url == null) {
                //不存在则返回
                return false;
            }
            responseHeader.contentType(MimeRegistry.lookupContentType(extension));
            is = url.openStream();
            int contentLength = is.available();
            protocol.writeResponseHeader(HttpServletResponse.SC_OK, contentLength);
            IOUtils.copy(is, rawOut);
            rawOut.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {

        }
    }
}
