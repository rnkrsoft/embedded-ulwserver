package com.rnkrsoft.embedded.ulwserver.server.handler;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.server.EmbeddedHttpConnection;
import com.rnkrsoft.embedded.ulwserver.server.message.BodyMessageRender;
import com.rnkrsoft.embedded.ulwserver.server.servlet.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 * Servlet处理器，用于实现Servlet协议规范
 */
@Slf4j
public class ServletHandler extends AbstractHandler {


    public ServletHandler(UlwServer server) {
        super(server);
    }

    @Override
    public boolean handleRequest(HttpConnection connection) throws IOException {
        OutputStream rawOut = connection.getRawOut();
        HttpProtocol protocol = connection.getProtocol();
        String urlPath = connection.getUri().getPath();
        final EmbeddedServletContext servletContext = new EmbeddedServletContext((EmbeddedHttpConnection) connection);
        //在容器中查找Servlet
        final Servlet servlet;
        try {
            servlet = ServletRegistry.lookupServlet(urlPath, servletContext);
        } catch (ServletException e) {
            if (log.isErrorEnabled()) {
                log.error("lookup servlet happens error!", e);
            }
            protocol.writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, BodyMessageRender.render(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, protocol, e));
            return true;
        }
        if (servlet == null) {
            if (log.isDebugEnabled()) {
                log.debug("servlet '{}' is not found!", urlPath);
            }
            protocol.writeError(HttpServletResponse.SC_NOT_FOUND, protocol.msg(HttpServletResponse.SC_NOT_FOUND));
            return false;
        }
        connection.setServlet(servlet);
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
                public void execute(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                    servlet.service(request, response);
                }
            });
            //执行过滤链，过滤链最终是执行业务Servlet代码
            filterChain.doFilter(new HttpServletRequestWrapper(servletRequest), new HttpServletResponseWrapper(servletResponse));
            //Servlet执行成功，则写入状态码和头部
            protocol.writeResponseHeader(servletResponse.getStatus(), servletResponse.getBufferSize(), EmbeddedCookieProcessor.generate(servletResponse.getCookies()));
            //容器自动刷新一次缓存的数据
            servletResponse.flushBuffer();
            rawOut.flush();
            return true;
        } catch (ServletException e) {
            if (log.isErrorEnabled()) {
                log.error("runAs servlet '" + servlet + "' happens error!", e);
            }
            protocol.writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, BodyMessageRender.render(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, protocol, e));
            return true;
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("write '" + servlet + "' happens error!", e);
            }
            protocol.writeError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, BodyMessageRender.render(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, protocol, e));
            return true;
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
