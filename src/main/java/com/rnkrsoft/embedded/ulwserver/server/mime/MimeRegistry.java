package com.rnkrsoft.embedded.ulwserver.server.mime;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 * 多用途互联网邮件扩展类型注册表
 */
public class MimeRegistry {
    final static Map<String, MimeMapping> MIME_MAPPINGS = new HashMap<String, MimeMapping>();
    final static MimeMapping octet_stream = new MimeMapping("*", "application/octet-stream");
    static {
        register("css", "text/css; charset=UTF-8");
        register("js", "application/x-javascript; charset=UTF-8");
        register("json", "application/json; charset=UTF-8");
        register("jpg", "image/jpeg");
        register("jpeg", "image/jpeg");
        register("gif", "image/gif");
        register("png", "image/png");
        register("ico", "image/x-icon");
        register("htm", "text/html; charset=UTF-8");
        register("html", "text/html; charset=UTF-8");
        register("xml", "text/xml; charset=UTF-8");
        register("svg", "image/svg+xml; charset=UTF-8");
        register("woff", "application/x-font-woff; charset=UTF-8");
        register("woff2", "application/octet-stream");
        register("eot", "application/vnd.ms-fontobject");
        register("ttf", "application/x-font-ttf");
    }

    /**
     * 注册
     * @param extension 拓展名
     * @param mimeType 多用途互联网邮件扩展类型名
     */
    public static void register(String extension, String mimeType){
        MIME_MAPPINGS.put(extension, new MimeMapping(extension, mimeType));
    }
    /**
     * 根据拓展名获取HTTP Content-Type
     * @param extension 拓展名
     * @return Content-Type字符串
     */
    public static String lookupContentType(String extension){
        MimeMapping mimeMapping = MIME_MAPPINGS.get(extension);
        if (mimeMapping == null){
            return octet_stream.getMimeType();
        }
        return mimeMapping.getMimeType();
    }
}
