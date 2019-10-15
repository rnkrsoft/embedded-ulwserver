package com.rnkrsoft.embedded.httpserver.server.webxml;

import com.rnkrsoft.embedded.httpserver.WebXml;

/**
 * Created by rnkrsoft.com on 2019/10/13.
 */
public class WebXmlParser {
    public WebXmlParser(String webXmlFileName) {
    }
    public boolean exists(){
        return false;
    }
    public WebXml parse(){
        WebXml webXml = new WebXml();
        //TODO 解析
        return webXml;
    }
}
