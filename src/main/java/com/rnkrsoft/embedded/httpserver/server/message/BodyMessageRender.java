package com.rnkrsoft.embedded.httpserver.server.message;

import com.rnkrsoft.embedded.httpserver.HttpProtocol;
import com.rnkrsoft.utils.StringUtils;

/**
 * Created by rnkrsoft.com on 2019/10/11.
 */
public class BodyMessageRender {
    public static String render(int code, HttpProtocol protocol, Throwable e){
        String temp = Integer.toString(code) + " ";
        temp += protocol.msg(code);
        temp += "\n\r" + StringUtils.toString(e);
        return temp;
    }

    public static String render(int code, String message, Throwable e){
        String temp = Integer.toString(code) + " ";
        temp += message;
        temp += "\n\r" + StringUtils.toString(e);
        return temp;
    }
}
