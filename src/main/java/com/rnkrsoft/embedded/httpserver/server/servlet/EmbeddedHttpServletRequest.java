package com.rnkrsoft.embedded.httpserver.server.servlet;

import com.rnkrsoft.embedded.httpserver.server.EmbeddedHttpConnection;
import com.rnkrsoft.embedded.httpserver.server.HttpHeader;
import com.rnkrsoft.embedded.httpserver.server.io.EndStreamCallback;
import com.rnkrsoft.embedded.httpserver.server.io.FixedLengthInputStream;
import com.rnkrsoft.io.buffer.ByteBuf;
import com.rnkrsoft.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.*;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Slf4j
public class EmbeddedHttpServletRequest implements HttpServletRequest {
    final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    String characterEncoding = "UTF-8";

    final Map<String, String[]> parameters = new LinkedHashMap();

    int serverPort = 80;

    final List<Locale> locales = new LinkedList();

    boolean asyncStarted = false;

    boolean asyncSupported = false;

    DispatcherType dispatcherType = DispatcherType.REQUEST;

    String authType;

    Cookie[] cookies;

    String pathInfo;


    String remoteUser;

    Principal userPrincipal;

    String requestedSessionId;


    @Getter
    String requestURI;
    @Getter
    String queryString = "";

    HttpSession session;

    boolean requestedSessionIdValid = true;

    boolean requestedSessionIdFromCookie = true;

    boolean requestedSessionIdFromURL = false;

    EmbeddedServletInputStream inputStream;

    final EmbeddedHttpConnection connection;

    final HttpHeader header;
    EmbeddedServletContext servletContext;

    ByteBuf byteBuf;

    public EmbeddedHttpServletRequest(final EmbeddedHttpConnection connection, final EmbeddedServletContext servletContext) {
        this.connection = connection;
        this.servletContext = servletContext;
        this.header = connection.getRequestHeader();
        this.byteBuf = ByteBuf.allocate(1024).autoExpand(true);
        URI uri = connection.getUri();
        this.requestURI = uri.getPath();
        this.queryString = uri.getQuery();
        this.queryString = this.queryString == null ? "" : this.queryString;

        String cookieString = connection.getRequestHeader().getCookie();
        List<Cookie> cookies = CookieProcessor.parse(cookieString);
        this.cookies = cookies.toArray(new Cookie[cookies.size()]);
        try {
            this.byteBuf.read(new FixedLengthInputStream(connection.getRawIn(), connection.getRequestHeader().getContentLength(), new EndStreamCallback() {
                @Override
                public void handle() {
                    //读取完请求的内容后，将连接置为应答状态
                    connection.requestState();
                }
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.inputStream = new EmbeddedServletInputStream(this.byteBuf);
        if (getMethod().equals("POST")) {
            try {
                this.queryString += this.byteBuf.getString(this.byteBuf.readableLength());
                this.queryString = URLDecoder.decode(this.queryString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                System.err.println("request chart encoding is unsupported!");
                throw new RuntimeException(e);
            }
            this.byteBuf.resetRead();
        }
        this.queryString = this.queryString == null ? "" : this.queryString;
        if (!this.queryString.isEmpty()) {
            String[] temps = this.queryString.split("&");
            for (String temp : temps) {
                String name = temp;
                String value = null;
                int idx = temp.indexOf("=");
                if (idx > -1) {
                    name = temp.substring(0, idx);
                    value = temp.substring(idx + 1);
                }
                setParameter(name, value);
            }
            if (log.isDebugEnabled()) {
                log.debug("\n" + StringUtils.asciiTable("Parameter Name", "Parameter Value", parameters));
            }
        }
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet(this.attributes.keySet()));
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public int getContentLength() {
        return inputStream.getByteBuf().capacity();
    }

    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public String getContentType() {
        return this.header.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() {
        return this.inputStream;
    }

    public void setParameter(String name, String value) {
        setParameter(name, new String[]{value});
    }

    public void setParameter(String name, String... values) {
        addParameter(name, values);
    }

    public void addParameter(String name, String... values) {
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        } else {
            this.parameters.put(name, values);
        }
    }

    @Override
    public String getParameter(String name) {
        String[] arr = (name != null ? this.parameters.get(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return (name != null ? this.parameters.get(name) : null);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    @Override
    public String getProtocol() {
        return this.servletContext.getSchema();
    }

    @Override
    public String getScheme() {
        return this.servletContext.getSchema();
    }

    @Override
    public String getServerName() {
        String host = header.host();
        if (host == null || "".equals(host.trim())) {
            host = connection.getLocalAddress();
        } else {
            if (host.startsWith("[")) {
                host = host.substring(1, host.indexOf(']'));
            } else if (host.indexOf(':') != -1) {
                host = host.substring(0, host.indexOf(':'));
            }
        }
        return host;
    }

    @Override
    public int getServerPort() {
        String host = getHeader("Host");
        if (host != null) {
            host = host.trim();
            int idx;
            if (host.startsWith("[")) {
                idx = host.indexOf(':', host.indexOf(']'));
            } else {
                idx = host.indexOf(':');
            }
            if (idx != -1) {
                return Integer.parseInt(host.substring(idx + 1));
            }
        }

        // else
        return this.serverPort;
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        InputStream sourceStream = new ByteArrayInputStream(this.inputStream.getByteBuf().getBytes(this.inputStream.getByteBuf().readableLength()));
        Reader sourceReader = (this.characterEncoding != null) ?
                new InputStreamReader(sourceStream, this.characterEncoding) :
                new InputStreamReader(sourceStream);
        return new BufferedReader(sourceReader);
    }

    @Override
    public String getRemoteAddr() {
        return this.connection.getRemoteAddress();
    }

    @Override
    public String getRemoteHost() {
        return this.connection.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        return this.locales.get(0);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    @Override
    public boolean isSecure() {
        return "https".equalsIgnoreCase(getScheme());
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return this.connection.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return this.connection.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return this.connection.getLocalAddress();
    }

    @Override
    public int getLocalPort() {
        return this.connection.getLocalPort();
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
        if (!this.asyncSupported) {
            throw new IllegalStateException("Async not supported");
        }
        this.asyncStarted = true;
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return this.asyncStarted;
    }

    @Override
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String getAuthType() {
        return this.authType;
    }

    public Cookie[] getCookies() {
        return this.cookies;
    }

    public long getDateHeader(String name) {
        return -1L;
    }

    public String getHeader(String name) {
        return header.getValue(name);
    }

    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(header.getValues(name));
    }

    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(header.getNames());
    }

    public int getIntHeader(String name) {
        String value = getHeader(name);
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return -1;
        }
    }

    public String getMethod() {
        return connection.getMethod();
    }

    public String getPathInfo() {
        return this.pathInfo;
    }

    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
    }

    public String getContextPath() {
        return this.servletContext.getContextPath();
    }

    public String getQueryString() {
        return this.queryString;
    }

    public String getRemoteUser() {
        return this.remoteUser;
    }

    public boolean isUserInRole(String role) {
        return false;
    }

    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer(this.getScheme()).append("://").append(this.getServerName());
        if (this.serverPort > 0 && (("http".equalsIgnoreCase(this.getScheme()) && this.serverPort != 80) ||
                ("https".equalsIgnoreCase(this.getScheme()) && this.serverPort != 443))) {
            url.append(':').append(this.serverPort);
        }
        String uri = getRequestURI();
        if (uri != null && !uri.isEmpty()) {
            url.append(getRequestURI());
        }
        return url;
    }

    public String getServletPath() {
        return this.servletContext.getServletPath();
    }

    public HttpSession getSession(boolean create) {
        return this.session;
    }

    public HttpSession getSession() {
        return getSession(true);
    }

    public String changeSessionId() {
        return this.session.getId();
    }


    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }


    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
    }


    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL;
    }

    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }


    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return null;
    }

    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        List<Part> result = new LinkedList<Part>();
        return result;
    }
}
