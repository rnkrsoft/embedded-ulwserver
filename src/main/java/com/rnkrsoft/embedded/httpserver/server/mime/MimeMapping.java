package com.rnkrsoft.embedded.httpserver.server.mime;

import lombok.Getter;

import java.io.Serializable;

/**
 * Created by rnkrsoft.com on 2019/10/15.
 * HTTP Mime映射
 */
public class MimeMapping implements Serializable{
    @Getter
    String extension;
    @Getter
    String mimeType;

    /**
     * 多用途互联网邮件扩展类型映射对象
     * @param extension 拓展名
     * @param mimeType 多用途互联网邮件扩展类型名
     */
    public MimeMapping(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }
}
