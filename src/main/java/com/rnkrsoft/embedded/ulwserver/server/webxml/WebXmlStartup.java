package com.rnkrsoft.embedded.ulwserver.server.webxml;

import com.rnkrsoft.embedded.ulwserver.UlwServer;
import com.rnkrsoft.embedded.ulwserver.WebXml;
import com.rnkrsoft.embedded.ulwserver.server.ServletMetadata;
import com.rnkrsoft.embedded.ulwserver.server.servlet.ServletRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 */
@Slf4j
public class WebXmlStartup {
    /**
     * 以WebXml对象设置容器
     *
     * @param webXml WebXml对象,内容格式参照Servlet规范中的Web.xml
     */
     void setting(UlwServer server, WebXml webXml) {
        if (webXml == null){
            log.warn("runAS non-web.xml");
        }
        List<WebXml.ServletTag> servletTags = webXml.getServlets();
        Map<String, WebXml.ServletTag> servletTagMap = new HashMap<String, WebXml.ServletTag>();
        for (WebXml.ServletTag servletTag : servletTags) {
            servletTagMap.put(servletTag.getServletName(), servletTag);
        }
        Map<String, List<WebXml.ServletMappingTag>> servletMappingTagMap = new HashMap<String, List<WebXml.ServletMappingTag>>();
        for (WebXml.ServletMappingTag servletMappingTag : webXml.getServletMappings()) {
            List<WebXml.ServletMappingTag> mappingTags = servletMappingTagMap.get(servletMappingTag.getServletName());
            if (mappingTags == null) {
                mappingTags = new ArrayList<WebXml.ServletMappingTag>();
            }
            mappingTags.add(servletMappingTag);
            servletMappingTagMap.put(servletMappingTag.getServletName(), mappingTags);
        }
        for (Map.Entry<String, WebXml.ServletTag> servletTagEntry : servletTagMap.entrySet()) {
            WebXml.ServletTag servletTag = servletTagEntry.getValue();
            List<WebXml.ServletMappingTag> mappingTags = servletMappingTagMap.get(servletTag.getServletName());
            ServletMetadata servletMetadata = ServletMetadata.builder()
                    .servletName(servletTag.getServletName())
                    .servletClass(servletTag.getServletClass())
                    .loadOnStartup(servletTag.getLoadOnStartup())
                    .build();
            for (WebXml.ServletMappingTag servletMappingTag : mappingTags) {
                servletMetadata.getUrlPatterns().add(servletMappingTag.getUrlPattern());
            }
            for (WebXml.ParamTag paramTag : servletTag.getInitParams()) {
                servletMetadata.getInitParams().put(paramTag.getParamName(), paramTag.getParamValue());
            }
            ServletRegistry.registerServlet(servletMetadata);
        }
        for (String index : webXml.getWelcomeFileList()) {
            server.getWelcomes().clear();
            server.getWelcomes().add(index);
        }
    }
}
