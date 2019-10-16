package com.rnkrsoft.embedded.ulwserver.server.servlet;

import com.rnkrsoft.embedded.ulwserver.server.ServletMetadata;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 */
public class ServletRegistry {
    /**
     * 注册的Servlet
     */
    static final Map<String, ServletMetadata> urlMappingServlets = new HashMap<String, ServletMetadata>();

    static final Set<ServletMetadata> servletMetadataPool = new HashSet<ServletMetadata>();

    static final Map<String, ? extends Servlet> servletPool = new HashMap<String, Servlet>();

    public static void registerServlet(ServletMetadata servletMetadata) {
        servletMetadataPool.add(servletMetadata);
        for (String urlPattern : servletMetadata.getUrlPatterns()) {
            urlMappingServlets.put(urlPattern, servletMetadata);
        }
    }
    /**
     * 根据映射url查找Servlet元信息
     * @param urlPattern url
     * @return 元信息
     */
    public static  ServletMetadata lookupServletMetadata(String urlPattern){
        return urlMappingServlets.get(urlPattern);
    }
    /**
     * 根据绑定的URL路径查询Servlet实例
     *
     * @param urlPattern URL路径
     * @param servletContext Servlet上下文
     * @return Servlet实例
     */
    public static Servlet lookupServlet(String urlPattern, ServletContext servletContext) throws ServletException {
        Servlet servlet = servletPool.get(urlPattern);
        if (servlet == null) {
            synchronized (servletPool) {
                if (servlet == null) {
                    final ServletMetadata servletMetadata = lookupServletMetadata(urlPattern);
                    if (servletMetadata == null) {
                        return servlet;
                    }
                    Class<? extends Servlet> servletClass = servletMetadata.getServletClass();
                    try {
                        if (servletClass.getConstructor() != null) {
                            servlet = servletClass.getConstructor().newInstance();
                            EmbeddedServletConfig servletConfig = new EmbeddedServletConfig((EmbeddedServletContext) servletContext, servletMetadata);
                            servlet.init(servletConfig);
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return servlet;
    }
}
