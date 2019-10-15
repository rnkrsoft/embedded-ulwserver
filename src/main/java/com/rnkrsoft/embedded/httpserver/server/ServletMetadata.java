package com.rnkrsoft.embedded.httpserver.server;

import lombok.Builder;
import lombok.Data;

import javax.servlet.Servlet;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 * Servlet元信息
 */
@Data
@Builder
public class ServletMetadata implements Serializable{
    /**
     * Servlet名称
     */
    String servletName;
    /**
     * servlet映射的url地址，支持多个
     */
    final List<String> urlPatterns = new ArrayList<String>();
    /**
     * Servlet类对象
     */
    Class<? extends Servlet> servletClass;
    /**
     * 初始化参数，用于Servlet.init(ServeltCnfig)时使用
     */
    final Map<String, String> initParams = new HashMap<String, String>();
    /**
     * 加载顺序
     */
    int loadOnStartup;
}
