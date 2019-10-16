package com.rnkrsoft.embedded.ulwserver.server.header;

import com.rnkrsoft.io.buffer.ByteBuf;
import com.rnkrsoft.utils.StringUtils;
import lombok.Getter;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * 此类用于封装HTTP头信息
 */
public class HttpHeader {
    static final String pattern = "EEE, dd MMM yyyy HH:mm:ss zzz";
    static final TimeZone gmtTZ = TimeZone.getTimeZone("GMT");

    static final String EXPECT = "Expect";
    static final String ACCEPT_RANGES = "Accept-Ranges";
    static final String AGE = "Age";
    static final String ETAG = "Etag";
    static final String LOCATION = "Location";
    static final String PROXY_AUTENTICATE = "Proxy-Autenticate";
    static final String RETRY_AFTER = "Retry-After";
    static final String SERVER = "Server";
    static final String VARY = "Vary";
    static final String COOKIE = "Cookie";
    static final String SET_COOKIE = "Set-Cookie";
    static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    static final String CONTENT_ENCODING = "Content-Encoding";
    static final String CONTENT_LANGUAGE = "Content-Language";
    static final String CONTENT_LENGTH = "Content-Length";
    static final String CONTENT_LOCATION = "Content-Location";
    static final String Content_MD5 = "Content-MD5";
    static final String CONTENT_RANGE = "Content-Range";
    static final String CONTENT_TYPE = "Content-Type";
    static final String EXPIRES = "Expires";
    static final String LAST_MODIFIED = "Last-Modified";
    static final String TRANSFER_ENCODING = "Transfer-Encoding";
    static final String DATE = "Date";
    static final String HOST = "Host";

    static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            DateFormat df = new SimpleDateFormat(pattern, Locale.US);
            df.setTimeZone(gmtTZ);
            return df;
        }
    };

    @Getter
    protected final RepeatableMap<String, String> headers = new RepeatableArrayMap<String, String>();

    public HttpHeader() {
        this.headers.set(CONTENT_TYPE, "text/html;charset=utf-8");
        this.headers.set(CONTENT_LENGTH, "0");
//        首先浏览器（也就是客户端）发送请求时，通过Accept-Encoding带上自己支持的内容编码格式列表；
//        服务端在接收到请求后，从中挑选出一种用来对响应信息进行编码，并通过Content-Encoding来说明服务端选定的编码信息
//        浏览器在拿到响应正文后，依据Content-Encoding进行解压。
//        服务端也可以返回未压缩的正文，但这种情况不允许返回Content-Encoding
//        this.headers.put(Content_Encoding, "");
        this.headers.set(SERVER, "UlwServer");
        this.headers.set(VARY, "Accept-Encoding");
    }

    public HttpHeader(Map<String, String> headers) throws IOException {
        this.headers.putAll(headers);
    }

    public int size() {
        return headers.size();
    }

    public Collection<String> getNames() {
        return headers.keys();
    }

    public List<String> getValues(String name) {
        List<String> values = new ArrayList<String>();
        Collection<String> values0 = headers.get(name);
        for (String value : values0){
            String[] temps = value.split("; ");
            values.addAll(Arrays.asList(temps));
        }
        return values;
    }


    public String getValue(String name){
        List<String> values = getValues(name);
        return values.isEmpty() ? null : values.get(0) ;
    }

    public void setValue(String name, String value){
        this.headers.set(name, value);
    }

    public void addValue(String name, String value){
        headers.put(name, value);
    }


    public String getTransferEncoding() {
        return getValue(TRANSFER_ENCODING);
    }

    /**
     * 刷新应答时间
     *
     * @return
     */
    public HttpHeader refreshDate() {
        headers.set(DATE, dateFormat.get().format(new Date()));
        return this;
    }

    public HttpHeader chunked() {
        headers.set(TRANSFER_ENCODING, "chunked");
        return this;
    }

    public long getContentLength() {
        return Long.valueOf(StringUtils.safeToString(getValue(CONTENT_LENGTH), "0"));
    }

    public HttpHeader contentLength(long contentLength) {
        headers.set(CONTENT_LENGTH, Long.toString(contentLength));
        return this;
    }

    public void write(OutputStream os) throws IOException {
        ByteBuf buffer = ByteBuf.allocate(1024).autoExpand(true);
        for (RepeatableMap.Entry<String, String> entry : headers.entries()) {
            buffer.putUTF8(entry.getKey());
            buffer.putUTF8(": ");
            buffer.putUTF8(entry.getValue());
            buffer.putUTF8("\r\n");
        }
        //连续两个\r\n时，Header部分结束
        buffer.putUTF8("\r\n");
        buffer.write(os);
    }


    public String getExpect() {
        return getValue(EXPECT);
    }


    public String getCookie(){
        return getValue(COOKIE);
    }

    public void addSetCookie(String setCookie){
        headers.put(SET_COOKIE, setCookie);
    }

    public List<String> getGetCookie(){
        return getValues(SET_COOKIE);
    }

    public HttpHeader contentType(String contentType) {
        headers.set(CONTENT_TYPE, contentType);
        return this;
    }

    @Override
    public String toString() {
        String[][] data = new String[headers.size()][2];
        int idx = 0;
        for (RepeatableMap.Entry<String, String> entry : headers.entries()) {
            data[idx][0] = entry.getKey();
            data[idx][1] = entry.getValue();
            idx++;
        }
        return "\n" + StringUtils.asciiTable(new String[]{"Header Name","Header Value"}, data);
    }

    public String getContentType() {
        return getValue(CONTENT_TYPE);
    }

    public String host() {
        return getValue(HOST);
    }
}
