package com.rnkrsoft.embedded.httpserver.server.handler;

import com.rnkrsoft.embedded.httpserver.HttpConnection;
import com.rnkrsoft.embedded.httpserver.HttpProtocol;
import com.rnkrsoft.embedded.httpserver.HttpServer;
import com.rnkrsoft.embedded.httpserver.server.EmbeddedHttpConnection;
import com.rnkrsoft.embedded.httpserver.server.ServletMetadata;
import com.rnkrsoft.embedded.httpserver.server.message.BodyMessageRender;
import com.rnkrsoft.embedded.httpserver.server.servlet.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 */
public class ServletHandler extends AbstractHandler{


    public ServletHandler(HttpServer server) {
        super(server);
    }

    @Override
    public boolean handleRequest(HttpConnection connection) throws IOException {
        OutputStream rawOut = connection.getRawOut();
        HttpProtocol protocol = connection.getProtocol();
        String urlPath = connection.getUri().getPath();
        final ServletMetadata servletMetadata = server.lookupServletMetadata(urlPath);
        //在容器中查找Servlet
        final Servlet servlet = server.lookupServlet(urlPath);
        if (servlet == null) {
            protocol.error(HttpServletResponse.SC_NOT_FOUND, protocol.msg(HttpServletResponse.SC_NOT_FOUND));
            return true;
        }
        connection.setServlet(servlet);
        final EmbeddedServletContext servletContext = new EmbeddedServletContext((EmbeddedHttpConnection) connection);
        //构建Servlet输入，输出流
        EmbeddedHttpServletRequest servletRequest = new EmbeddedHttpServletRequest((EmbeddedHttpConnection) connection, servletContext);
        EmbeddedHttpServletResponse servletResponse = new EmbeddedHttpServletResponse((EmbeddedHttpConnection) connection, servletContext);
        servletResponse.setRequest(servletRequest);

        //构建过滤链
        List<Filter> systemFilters = connection.getSystemFilters();
        List<Filter> userFilters = connection.getUserFilters();
        List<Filter> allFilters = new ArrayList<Filter>(systemFilters.size() + userFilters.size());
        allFilters.addAll(userFilters);
        allFilters.addAll(systemFilters);
        try {
            //将所有过滤器和业务servlet构建成过滤链
            EmbeddedFilterChain filterChain = new EmbeddedFilterChain(allFilters.listIterator(), new ExecuteServletCallback() {
                @Override
                public void execute(ServletRequest request, ServletResponse response) {
                    EmbeddedServletConfig servletConfig = new EmbeddedServletConfig(servletContext, servletMetadata);
                    try {
                        servlet.init(servletConfig);
                        servlet.service(request, response);
                    } catch (ServletException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            });
            //执行过滤链，过滤链最终是执行业务Servlet代码
            filterChain.doFilter(new HttpServletRequestWrapper(servletRequest), new HttpServletResponseWrapper(servletResponse));
            //Servlet执行成功，则写入状态码和头部
            protocol.responseHeader(servletResponse.getStatus(), servletResponse.getBufferSize(), CookieProcessor.generate(servletResponse.getCookies()));
            //容器自动刷新一次缓存的数据
            servletResponse.flushBuffer();
            rawOut.flush();
            return true;
        } catch (ServletException e) {
            protocol.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, BodyMessageRender.render(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, protocol, e));
            return true;
        } catch (IOException e) {
            protocol.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, BodyMessageRender.render(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, protocol, e));
            return true;
        }
    }
}
