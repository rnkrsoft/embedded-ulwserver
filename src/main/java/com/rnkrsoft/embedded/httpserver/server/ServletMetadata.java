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
 */
@Data
@Builder
public class ServletMetadata implements Serializable{
    String servletName;
    final List<String> urlPatterns = new ArrayList<String>();
    Class<? extends Servlet> servletClass;
    final Map<String, String> initParams = new HashMap<String, String>();
    int loadOnStartup;
}
