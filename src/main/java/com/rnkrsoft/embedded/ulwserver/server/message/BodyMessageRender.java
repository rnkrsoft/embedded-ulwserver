package com.rnkrsoft.embedded.ulwserver.server.message;

import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.utils.StringUtils;

/**
 * Created by rnkrsoft.com on 2019/10/11.
 * HTTP BODY内容渲染器
 */
public class BodyMessageRender {
    /**
     * 根据输入的应答码和异常进行渲染
     * @param code 应答码
     * @param protocol 协议对象
     * @param e 异常
     * @return 文本内容
     */
    public static String render(int code, HttpProtocol protocol, Throwable e){
        String temp = Integer.toString(code) + " ";
        temp += protocol.msg(code);
        temp += "\n\r" + StringUtils.toString(e);
        return temp;
    }
    /**
     * 根据输入的应答码和异常进行渲染
     * @param code 应答码
     * @param message 文本提示信息
     * @param e 异常
     * @return 文本内容
     */
    public static String render(int code, String message, Throwable e){
        String temp = Integer.toString(code) + " ";
        temp += message;
        temp += "\n\r" + StringUtils.toString(e);
        return temp;
    }
}
