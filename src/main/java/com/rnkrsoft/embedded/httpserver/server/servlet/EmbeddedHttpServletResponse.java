package com.rnkrsoft.embedded.httpserver.server.servlet;

import com.rnkrsoft.embedded.httpserver.server.EmbeddedHttpConnection;
import com.rnkrsoft.embedded.httpserver.server.HttpHeader;
import com.rnkrsoft.io.buffer.ByteBuf;
import com.rnkrsoft.message.MessageFormatter;
import com.rnkrsoft.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Slf4j
@Data
public class EmbeddedHttpServletResponse implements HttpServletResponse {
    boolean outputStreamAccessAllowed = true;

    boolean writerAccessAllowed = true;

    String characterEncoding = "UTF-8";

    boolean charset = false;

    ByteArrayOutputStream content = new ByteArrayOutputStream(1024);

    String contentType;

    boolean committed;

    Locale locale = Locale.getDefault();

    final List<Cookie> cookies = new ArrayList();

    int status = HttpServletResponse.SC_OK;

    String errorMessage;

    String forwardedUrl;

    final List<String> includedUrls = new ArrayList();
    ByteBuf headerBuffer;
    ByteBuf bodyBuffer;

    EmbeddedHttpConnection httpConnection;
    EmbeddedHttpServletRequest request;

    HttpHeader header;
    boolean flushBuffer;

    public EmbeddedHttpServletResponse(EmbeddedHttpConnection httpConnection, final EmbeddedServletContext servletContext) {
        this.httpConnection = httpConnection;
        this.bodyBuffer = ByteBuf.allocate(1024).autoExpand(true);
        this.header = httpConnection.getResponseHeader();
        this.header.contentType("text/html; charset=UTF-8");
        setStatus(200);
    }

    public void write(String msg) {
        this.bodyBuffer.put(Charset.forName(characterEncoding), msg);
    }

    public void write(byte[] data) {
        this.bodyBuffer.put(data);
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public boolean containsHeader(String name) {
        return this.header.getHeaders().containsKey(name) > 0;
    }

    public String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("不支持的编码");
        }
    }

    public String encodeRedirectURL(String url) {
        return encodeUrl(url);
    }

    public String encodeUrl(String url) {
        return encodeUrl(url);
    }

    public String encodeRedirectUrl(String url) {
        return encodeUrl(url);
    }

    public void sendError(int sc, String msg) throws IOException {
        setStatus(sc);
    }

    public void sendError(int sc) throws IOException {
        setStatus(sc);
    }

    public void sendRedirect(String location) throws IOException {
        //通过301进行重定向
        setStatus(301);
        String location0 = location;
        if (!location.toLowerCase().startsWith("http://") && !location.toLowerCase().startsWith("https://")) {
            if (location.startsWith("/")) {
                location0 = MessageFormatter.format("{}://{}:{}{}", request.getScheme(), request.getServerName(), request.getServerPort(), location);
            } else {
                location0 = MessageFormatter.format("{}://{}:{}/{}/{}", request.getScheme(), request.getServerName(), request.getServerPort(), location);
            }
        }
        setHeader("Location", location0);
    }

    public void setDateHeader(String name, long date) {

    }

    public void addDateHeader(String name, long date) {

    }

    public void setHeader(String name, String value) {
        addHeader(name, value);

    }

    public void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            setContentType(value);
        } else {
            doAddHeaderValue(name, value);
        }
    }

    void doAddHeaderValue(String name, Object value) {
        this.header.setValue(name, StringUtils.safeToString(value));
    }

    public void setIntHeader(String name, int value) {
        setHeader(name, Integer.toString(value));
    }

    public void addIntHeader(String name, int value) {
        setHeader(name, Integer.toString(value));
    }

    public void setStatus(int sc, String sm) {
        this.status = sc;
        this.errorMessage = sm;
    }

    public String getHeader(String name) {
        return this.header.getValue(name);
    }

    public Collection<String> getHeaders(String name) {
        return this.header.getValues(name);
    }

    public Collection<String> getHeaderNames() {
        return this.header.getNames();
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new EmbeddedServletOutputStream(this.bodyBuffer);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        PrintWriter writer = new PrintWriter(getOutputStream());
        return writer;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {
        this.bodyBuffer.capacity(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        this.bodyBuffer.capacity((int) len);
    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return bodyBuffer.readableLength();
    }


    @Override
    public void flushBuffer() throws IOException {
        if (log.isDebugEnabled()){
            log.debug("\n response header:" + header);
        }
        if (flushBuffer){
            return;
        }
        flushBuffer = true;
        //将封装的Servlet的数据写入原始输出流
        bodyBuffer.write(httpConnection.getRawOut());
    }

    @Override
    public void resetBuffer() {
        this.bodyBuffer.clear();
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
        this.bodyBuffer.clear();
    }
}
