package com.rnkrsoft.embedded.ulwserver.server.servlet;

import com.rnkrsoft.embedded.ulwserver.server.ServletMetadata;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class EmbeddedServletConfig implements ServletConfig{
    EmbeddedServletContext servletContext;
    ServletMetadata servletMetadata;

    public EmbeddedServletConfig(EmbeddedServletContext servletContext, ServletMetadata servletMetadata) {
        this.servletContext = servletContext;
        this.servletMetadata = servletMetadata;
    }

    @Override
    public String getServletName() {
        return servletMetadata.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return servletMetadata.getInitParams().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(servletMetadata.getInitParams().keySet());
    }
}
