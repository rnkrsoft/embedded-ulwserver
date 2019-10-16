package com.rnkrsoft.embedded.ulwserver;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Data
public class WebXml {
    String displayName;
    final List<ParamTag> contextParams = new ArrayList<ParamTag>();
    final List<ServletTag> servlets = new ArrayList<ServletTag>();
    final List<ServletMappingTag> servletMappings = new ArrayList<ServletMappingTag>();
    final List<FilterTag> filters = new ArrayList<FilterTag>();
    final List<FilterMappingTag> filterMappings = new ArrayList<FilterMappingTag>();
    final List<String> welcomeFileList = new ArrayList();

    @Data
    public static class ParamTag {
        String paramName;
        String paramValue;
    }


    @Data
    public static class ServletTag{
        String servletName;
        Class servletClass;
        final List<ParamTag> initParams = new ArrayList<ParamTag>();
        int loadOnStartup;
    }

    @Data
    public static class ServletMappingTag{
        String servletName;
        String urlPattern;
    }
    @Data
    public static class FilterTag{
        String servletName;
        Class servletClass;
        final List<ParamTag> initParams = new ArrayList<ParamTag>();
        int loadOnStartup;
    }
    @Data
    public static class FilterMappingTag{
        String filterName;
        String urlPattern;
    }
}
