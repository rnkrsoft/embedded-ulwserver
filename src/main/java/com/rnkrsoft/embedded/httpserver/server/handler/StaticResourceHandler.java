package com.rnkrsoft.embedded.httpserver.server.handler;


import com.rnkrsoft.embedded.httpserver.HttpConnection;
import com.rnkrsoft.embedded.httpserver.HttpHandler;
import com.rnkrsoft.embedded.httpserver.HttpProtocol;
import com.rnkrsoft.embedded.httpserver.HttpServer;
import com.rnkrsoft.embedded.httpserver.server.HttpHeader;
import com.rnkrsoft.embedded.httpserver.server.io.IOUtils;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 */
public class StaticResourceHandler extends AbstractHandler {

    public StaticResourceHandler(HttpServer server) {
        super(server);
    }

    @Override
    public boolean handleRequest(HttpConnection connection) throws IOException {
        return staticResource(connection);
    }

    boolean staticResource(HttpConnection connection) throws IOException {
        HttpProtocol protocol = connection.getProtocol();
        HttpHeader responseHeader = connection.getResponseHeader();
        OutputStream rawOut = connection.getRawOut();
        //路径
        String path = "";
        //文件名
        String fileName = connection.getUri().getPath();
        if (fileName.equals("/")) {//如果是首页，则遍历设置的，看是否能够加载成功
            List<String> welcomes = connection.getServer().getWelcomes();
            for (String welcome : welcomes) {
                if (loadStaticResource(connection, protocol, responseHeader, rawOut, path, welcome)) {
                    return true;
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

    private boolean loadStaticResource(HttpConnection connection, HttpProtocol protocol, HttpHeader responseHeader, OutputStream rawOut, String path, String fileName) {
        InputStream is;
        try {
            int lastFilePos = fileName.lastIndexOf(".");
            String suffix = "";
            //文件资源必须有后缀名
            if (lastFilePos > -1) {
                suffix = fileName.substring(lastFilePos + 1);
                suffix = suffix.toLowerCase();
            }
            String name = connection.getServer().getConfig().getString("WEB_ROOT", "META-INF/resources") + "/";
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
            if ("css".equals(suffix)) {
                responseHeader.contentType("text/css; charset=UTF-8");
            } else if ("js".equals(suffix)) {
                responseHeader.contentType("application/x-javascript; charset=UTF-8");
            } else if ("jpg".equals(suffix) || "jpeg".equals(suffix)) {
                responseHeader.contentType("image/jpeg");
            } else if ("gif".equals(suffix)) {
                responseHeader.contentType("image/gif");
            } else if ("png".equals(suffix)) {
                responseHeader.contentType("image/png");
            } else if ("ico".equals(suffix)) {
                responseHeader.contentType("image/x-icon");
            } else if ("htm".equals(suffix) || "html".equals(suffix)) {
                responseHeader.contentType("text/html; charset=UTF-8");
            } else if ("xml".equals(suffix)) {
                responseHeader.contentType("text/xml; charset=UTF-8");
            } else if ("svg".equals(suffix)) {
                responseHeader.contentType("image/svg+xml; charset=UTF-8");
            } else if ("svg".equals(suffix)) {
                responseHeader.contentType("image/svg+xml; charset=UTF-8");
            } else if ("woff".equals(suffix)) {
                responseHeader.contentType("application/x-font-woff; charset=UTF-8");
            } else if ("woff2".equals(suffix)) {
                responseHeader.contentType("application/octet-stream");
            } else if ("eot".equals(suffix)) {
                responseHeader.contentType("application/vnd.ms-fontobject");
            } else if ("ttf".equals(suffix)) {
                responseHeader.contentType("application/x-font-ttf");
            } else {
                responseHeader.contentType("application/octet-stream");
            }
            is = url.openStream();
            int contentLength = is.available();
            protocol.responseHeader(HttpServletResponse.SC_OK, contentLength);
            IOUtils.copy(is, rawOut);
            rawOut.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {

        }
    }
}
