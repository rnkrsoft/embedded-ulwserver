package com.rnkrsoft.embedded.ulwserver;


import com.rnkrsoft.embedded.ulwserver.server.header.HttpHeader;
import com.rnkrsoft.io.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * HTTP连接器
 */
@Slf4j
public abstract class HttpProtocol {
    public static final String HTTP_VERSION_09 = "HTTP/0.9";
    public static final String HTTP_VERSION_10 = "HTTP/1.0";
    public static final String HTTP_VERSION_11 = "HTTP/1.1";
    public static final byte CR = 13;
    public static final byte LF = 10;

    public static final String HEAD = "HEAD";
    public static final String GET = "GET";
    public static final String POST = "POST";

    public void handle(HttpConnection connection) throws IOException, URISyntaxException {
        InputStream rawIn = connection.getRawIn();
        OutputStream rawOut = connection.getRawOut();
        String startLine;
        do {
            startLine = readFirstLine(rawIn);
            if (startLine == null) {
                return;
            }
        } while (startLine == null ? false : startLine.equals(""));
        log.debug("head:{}", startLine);

        //此时startLine 为GET /images/logo.gif HTTP/1.1
        //如果构建出的请求对象，读取信息为空，则关闭连接
        if (startLine == null) {
            connection.close();
            return;
        }
        //如果读取信息无空格，则为无效请求
        int space = startLine.indexOf(' ');
        if (space == -1) {
            writeError(HttpServletResponse.SC_NOT_FOUND, "Bad request");
            return;
        }
        //如果字符串开始到第一个空格位置，则为GET
        String method = startLine.substring(0, space);
        int start = space + 1;
        //如果读取第一个空格之后的信息无空格，则为无效请求
        space = startLine.indexOf(' ', start);
        if (space == -1) {
            writeError(HttpServletResponse.SC_NOT_FOUND, "Bad request");
            return;
        }
        //uri则为第一个空格和第二个空格之间的内容，则为/images/logo.gif
        String uriStr = startLine.substring(start, space);

        start = space + 1;
        //解析第二个空格之后，则为HTTP/1.1
        String version = startLine.substring(start);

        connection.setMethod(method);
        connection.setVersion(version);
        String url = getName() + "://" + connection.getLocalName() + ":" + connection.getLocalPort() + (uriStr.length() == 0 ? "/" : uriStr);
        connection.setUri(new URI(url));
        //解析请求Header
        connection.setRequestHeader(new HttpHeader(parseHeader(rawIn)));
        connection.setResponseHeader(new HttpHeader());
        if (log.isDebugEnabled()){
            log.debug(connection.getRequestHeader().toString());
        }
        handle0(connection);
    }

    /**
     * 读取第一行
     *
     * @param rawIn 原生输入流
     * @return 读取第一行内容
     * @throws IOException IO异常
     */
    String readFirstLine(InputStream rawIn) throws IOException {
        int c;
        ByteBuf buffer = ByteBuf.allocate(512).autoExpand(true);
        boolean foundCR = false;
        while ((c = rawIn.read()) != -1) {
            if (c == CR) {
                foundCR = true;
                continue;
            }
            if (foundCR && c == LF) {
                break;
            }
            buffer.put((byte) c);
        }
        return buffer.getString(Charset.forName("UTF-8"), buffer.readableLength());
    }

    /**
     * 解析头信息
     *
     * @param rawIn 原生输入流
     * @return 头信息键值对
     * @throws IOException IO异常
     */
    public Map<String, String> parseHeader(InputStream rawIn) throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        ByteBuf name = ByteBuf.allocate(1024).autoExpand(true);
        ByteBuf value = ByteBuf.allocate(1024).autoExpand(true);
        int c = rawIn.read();
        if (c == CR) {
            c = rawIn.read();
            if (c == LF) {
                return headers;
            } else {//第一个非换行符
                name.put((byte) c);
            }
        }else{//第一个非换行符
            name.put((byte) c);
        }
        boolean finishName = false;
        while ((c = rawIn.read()) > -1){
            if (c == ':'){//如果是头信息键和值分割符，则将finishName设置为真
                c = rawIn.read();
                if (c == ' ' || c == '\t') {
                    finishName = true;
                }else{
                    //FIXME 需要保存这个字符吗？
                }
            }else if (finishName){//完成头信息键的获取，如果出现换行符则是这组参数结束；否则就是参数的值
                if (c == CR){
                    c = rawIn.read();
                    if (c == LF){
                        headers.put(name.getString(Charset.forName("UTF-8"), name.readableLength()), value.getString(Charset.forName("UTF-8"), value.readableLength()));
                        name.clear();
                        value.clear();
                        finishName = false;
                        c = rawIn.read();
                        if (c == CR) {
                            c = rawIn.read();
                            if (c == LF) {
                                break;
                            } else {//可能是第二个及其之后的参数名第一个字母
                                name.put((byte) c);
                            }
                        }else{
                            //可能是第二个及其之后的参数名第一个字母
                            name.put((byte) c);
                        }
                    }else{
                        value.put((byte)c); //FIXME 需要保存这个字符吗？
                    }
                }else{
                    value.put((byte)c);
                }
            }else if (!finishName){
                name.put((byte) c);
            }
        }
        return headers;
    }

    /**
     * 处理连接
     *
     * @param connection 连接对象
     */
    protected abstract void handle0(HttpConnection connection) throws IOException;

    /**
     * 写入应答头信息
     *
     * @param code          应答代码
     * @param contentLength 内容长度
     */
    public abstract void writeResponseHeader(int code, int contentLength, List<String> setCookies) throws IOException;

    /**
     * 写入应答头信息到输出流
     * @param code          应答代码
     * @param contentLength 内容长度
     * @throws IOException 有可能发生写入异常，客户端主动关闭
     */
    public abstract void writeResponseHeader(int code, int contentLength) throws IOException;

    /**
     * 回收协议，用于重复使用
     */
    public abstract void recycle();

    /**
     * 应答码转换为应答信息
     * @param code 应答码
     * @return 应答信息
     */
    public abstract String msg(int code);

    /**
     * 向输出流写入成功头信息，1xx,2xx,3xx都属于
     * @param code 应答码
     * @param text 应答信息
     */
    public abstract void writeSuccess(int code, String text);

    /**
     * 向输出流写入错误头信息，4xx都属于
     * @param code 应答码
     * @param text 应答信息
     */
    public abstract void writeError(int code, String text);

    /**
     * 协议名称
     * @return 返回协议名称
     */
    public abstract String getName();

    /**
     * 协议版本号
     * @return 返回协议版本号
     */
    public abstract String getVersion();
}
