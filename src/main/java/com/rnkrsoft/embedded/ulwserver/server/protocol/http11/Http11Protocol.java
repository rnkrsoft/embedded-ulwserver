package com.rnkrsoft.embedded.ulwserver.server.protocol.http11;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.HttpHandler;
import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.embedded.ulwserver.server.event.HandleFinishEvent;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Slf4j
public class Http11Protocol extends HttpProtocol {
    HttpConnection connection;

    boolean writeResponseHeader;


    protected void handle0(HttpConnection connection) throws IOException {
        try {
            if (!HTTP_VERSION_11.equals(connection.getVersion())) {
                writeError(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED, "http protocol version " + connection.getVersion() + "is not supported!");
                return;
            }
            this.connection = connection;
            this.connection.setProtocol(this);
            handle1(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.connection.finish();
        }
    }

    void handle1(HttpConnection connection) throws IOException, URISyntaxException {
        String s = connection.getRequestHeader().getTransferEncoding();
        //传输长度
        long contentLength = 0L;
        //分块编码传输
        if (s != null && s.equalsIgnoreCase("chunked")) {
            contentLength = -1L;
        } else {
            contentLength = connection.getRequestHeader().getContentLength();
            if (contentLength == 0) {
//                connection.idleState();
            }
        }
        connection.getRequestHeader().contentLength(contentLength);

        //Expect请求头域用于指明客户端需要的特定服务器行为
        String exp = connection.getRequestHeader().getExpect();
        //客户端发送超过1024字节请求时
        //1.会先发送一个请求, 包含一个Expect:100-continue, 询问Server使用愿意接受数据，
        //2.接收到Server返回的100-continue应答以后, 才把数据POST给Server
        if (exp != null && exp.equalsIgnoreCase("100-continue")) {
            //TODO
            writeSuccess(HttpServletResponse.SC_CONTINUE, "");
        }
        //首先进行静态资源查找，如果没有，则访问Servlet
        HttpHandler[] handlers = connection.getServer().getHandlers();
        for (int i = 0; i < handlers.length; i++) {
            if (handlers[i].handleRequest(connection)){
                return;
            }
        }
        writeError(HttpServletResponse.SC_NOT_FOUND, "Bad request!");
    }
    public void writeResponseHeader(int code, int contentLength, List<String> setCookies) throws IOException {
        //向客户端写入头信息
        for (String setCookie : setCookies){
            connection.getResponseHeader().addSetCookie(setCookie);
        }
        writeResponseHeader(code, contentLength);
    }

    public void writeResponseHeader(int code, int contentLength) throws IOException {
        if (writeResponseHeader) {
            throw new IOException("HTTP headers has already write");
        }
        boolean noContentToSend = false;
        OutputStream rawOut = connection.getRawOut();
        String statusLine = connection.getVersion() + ' ' + code + ' ' + msg(code) + "\r\n";
        //写入应答协议
        rawOut.write(statusLine.getBytes("UTF-8"));
        connection.getResponseHeader().refreshDate();
        if ((code >= HttpServletResponse.SC_CONTINUE && code < HttpServletResponse.SC_OK)
                || (code == HttpServletResponse.SC_NO_CONTENT)
                || (code == HttpServletResponse.SC_NOT_MODIFIED)) {
            if (contentLength != -1) {
                log.info("writeResponseHeader: code = {}: forcing contentLen = -1", code);
            }
            contentLength = -1;
        }
        //如果Method:Head，则不应该有内容长度，所以为0
        if (HEAD.equals(connection.getMethod())) {
            if (contentLength >= 0) {
                log.info("sendResponseHeaders: being invoked with a content length for a HEAD request");
            }
            noContentToSend = true;
            contentLength = 0;
        } else {//如果不是Method:Head
            if (contentLength == 0) {
                connection.getResponseHeader().chunked();
            } else {
                if (contentLength == -1) {
                    noContentToSend = true;
                    contentLength = 0;
                }
                connection.getResponseHeader().contentLength(contentLength);
            }
        }
        connection.getResponseHeader().write(rawOut);
        //将实际的内容长度保存在连接器上
        connection.getResponseHeader().contentLength(contentLength);
        //设置为已向客户端写入头信息
        this.writeResponseHeader = true;
        //如果没有内容，则创建完成事件
        if (noContentToSend) {
            HandleFinishEvent event = new HandleFinishEvent(connection);
            connection.getServer().publishEvent(event);
        }
    }

    @Override
    public void recycle() {
        connection = null;
        writeResponseHeader = false;
    }

    public void writeSuccess(int code, String text) {
        assert code >= 100 && code < 300;
        sendResult(code, text);
    }

    public void writeError(int code, String text) {
        assert code >= 400 && code < 600;
        sendResult(code, text);
    }

    @Override
    public String getName() {
        return "http";
    }

    @Override
    public String getVersion() {
        return "1.1";
    }

    void sendResult(int code, String text) {
        try {
            StringBuilder builder = new StringBuilder(512);
            builder.append("HTTP/1.1 ").append(code).append(' ').append(msg(code)).append("\r\n");
            builder.append("Content-Length: ").append(text.length()).append("\r\n");
            builder.append("Content-Type: ").append("text/plain; charset=utf-8").append("\r\n");
            builder.append("Connection: close\r\n");
            builder.append("\r\n").append(text);
            String s = builder.toString();
            if (log.isDebugEnabled()) {
                log.debug(s);
            }
            byte[] b = s.getBytes("ISO8859_1");
            OutputStream rawOut = connection.getRawOut();
            rawOut.write(b);
            rawOut.flush();
        } catch (IOException e) {
            log.error("ServerImpl.sendReply", e);
        }
    }

    public String msg(int code) {

        switch (code) {
            case HttpServletResponse.SC_OK:
                return "OK";
            case HttpServletResponse.SC_CONTINUE:
                return "Continue";
            case HttpServletResponse.SC_CREATED:
                return "Created";
            case HttpServletResponse.SC_ACCEPTED:
                return "Accepted";
            case HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION:
                return "Non-Authoritative Information";
            case HttpServletResponse.SC_NO_CONTENT:
                return "No Content";
            case HttpServletResponse.SC_RESET_CONTENT:
                return "Reset Content";
            case HttpServletResponse.SC_PARTIAL_CONTENT:
                return "Partial Content";
            case HttpServletResponse.SC_MULTIPLE_CHOICES:
                return "Multiple Choices";
            case HttpServletResponse.SC_MOVED_PERMANENTLY:
                return "Moved Permanently";
            case HttpServletResponse.SC_MOVED_TEMPORARILY:
                return "Temporary Redirect";
            case HttpServletResponse.SC_SEE_OTHER:
                return "See Other";
            case HttpServletResponse.SC_NOT_MODIFIED:
                return "Not Modified";
            case HttpServletResponse.SC_USE_PROXY:
                return "Use Proxy";
            case HttpServletResponse.SC_BAD_REQUEST:
                return "Bad Request";
            case HttpServletResponse.SC_UNAUTHORIZED:
                return "Unauthorized";
            case HttpServletResponse.SC_PAYMENT_REQUIRED:
                return "Payment Required";
            case HttpServletResponse.SC_FORBIDDEN:
                return "Forbidden";
            case HttpServletResponse.SC_NOT_FOUND:
                return "Not Found";
            case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
                return "Method Not Allowed";
            case HttpServletResponse.SC_NOT_ACCEPTABLE:
                return "Not Acceptable";
            case HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED:
                return "Proxy Authentication Required";
            case HttpServletResponse.SC_REQUEST_TIMEOUT:
                return "Request Time-Out";
            case HttpServletResponse.SC_CONFLICT:
                return "Conflict";
            case HttpServletResponse.SC_GONE:
                return "Gone";
            case HttpServletResponse.SC_LENGTH_REQUIRED:
                return "Length Required";
            case HttpServletResponse.SC_EXPECTATION_FAILED:
                return "Precondition Failed";
            case HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE:
                return "Request Entity Too Large";
            case HttpServletResponse.SC_REQUEST_URI_TOO_LONG:
                return "Request-URI Too Large";
            case HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE:
                return "Unsupported Media Type";
            case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
                return "Internal Server Error";
            case HttpServletResponse.SC_NOT_IMPLEMENTED:
                return "Not Implemented";
            case HttpServletResponse.SC_BAD_GATEWAY:
                return "Bad Gateway";
            case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
                return "Service Unavailable";
            case HttpServletResponse.SC_GATEWAY_TIMEOUT:
                return "Gateway Timeout";
            case HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED:
                return "HTTP Version Not Supported";
            default:
                return "";
        }
    }
}
