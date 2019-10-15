package com.rnkrsoft.embedded.httpserver.server.servlet;

import com.rnkrsoft.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 * Cookie处理器
 */
@Slf4j
public class EmbeddedCookieProcessor {
    /**
     * 将Cookie字符串解析为Cookie数组
     * @param cookieString 从HTTP头信息中获取的Cookie字符串
     * @return Cookie数组
     */
    public static List<Cookie> parse(String cookieString) {
        List<Cookie> cookies = parseCookieRfc6265(cookieString);
        return cookies;
    }

    public static List<String> generate(List<Cookie> cookies) {
        List<String> setCookies = new ArrayList<String>();
        for (Cookie cookie : cookies) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");

            if (StringUtils.isNotBlank(cookie.getPath())) {
                buffer.append("Path=").append(cookie.getPath()).append("; ");
            }
            if (StringUtils.isNotBlank(cookie.getDomain())) {
                buffer.append("Domain=").append(cookie.getDomain()).append("; ");
            }
            buffer.append("Version=").append(cookie.getVersion()).append("; ");
            if (StringUtils.isNotBlank(cookie.getMaxAge())) {
                buffer.append("Max-Age=").append(cookie.getMaxAge()).append("; ");
            }
            setCookies.add(buffer.toString());
        }
        return setCookies;
    }

    /**
     * 按照RFC6265协议进行Cookie的解析
     * @param cookieString Cookie字符串
     * @return Cookie数组
     */
    static List<Cookie> parseCookieRfc6265(String cookieString) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        if (cookieString == null) {
            return cookies;
        }
        String name = null;
        String value = null;
        int version = 0;

        Cookie cookie = null;

        boolean inValue = false;
        boolean quoted = false;
        boolean escaped = false;
        int beginIndex = -1;
        int endIndex = -1;
        for (int i = 0, length = cookieString.length(), last = length - 1; i < length; i++) {
            char c = cookieString.charAt(i);
            if (quoted) {
                if (escaped) {
                    escaped = false;
                    continue;
                }

                switch (c) {
                    case '"':
                        endIndex = i;
                        quoted = false;

                        if (i == last) {
                            if (inValue)
                                value = cookieString.substring(beginIndex, endIndex + 1);
                            else {
                                name = cookieString.substring(beginIndex, endIndex + 1);
                                value = "";
                            }
                        }
                        break;

                    case '\\':
                        escaped = true;
                        continue;
                    default:
                        continue;
                }
            } else {
                if (inValue) {
                    switch (c) {
                        case ' ':
                        case '\t':
                            continue;

                        case '"':
                            if (beginIndex < 0) {
                                quoted = true;
                                beginIndex = i;
                            }
                            endIndex = i;
                            if (i == last) {
                                value = cookieString.substring(beginIndex, endIndex + 1);
                                break;
                            }
                            continue;

                        case ';':
                            // case ',':
                            if (beginIndex >= 0)
                                value = cookieString.substring(beginIndex, endIndex + 1);
                            else
                                value = "";
                            beginIndex = -1;
                            inValue = false;
                            break;

                        default:
                            if (beginIndex < 0)
                                beginIndex = i;
                            endIndex = i;
                            if (i == last) {
                                value = cookieString.substring(beginIndex, endIndex + 1);
                                break;
                            }
                            continue;
                    }
                } else {
                    switch (c) {
                        case ' ':
                        case '\t':
                            continue;

                        case '"':
                            if (beginIndex < 0) {
                                quoted = true;
                                beginIndex = i;
                            }
                            endIndex = i;
                            if (i == last) {
                                name = cookieString.substring(beginIndex, endIndex + 1);
                                value = "";
                                break;
                            }
                            continue;

                        case ';':
                            // case ',':
                            if (beginIndex >= 0) {
                                name = cookieString.substring(beginIndex, endIndex + 1);
                                value = "";
                            }
                            beginIndex = -1;
                            break;

                        case '=':
                            if (beginIndex >= 0) {
                                name = cookieString.substring(beginIndex, endIndex + 1);
                            }
                            beginIndex = -1;
                            inValue = true;
                            continue;

                        default:
                            if (beginIndex < 0)
                                beginIndex = i;
                            endIndex = i;
                            if (i == last) {
                                name = cookieString.substring(beginIndex, endIndex + 1);
                                value = "";
                                break;
                            }
                            continue;
                    }
                }
            }

            if (value != null && name != null) {
                name = unquote(name, false);
                value = unquote(value, false);
                try {
                    if (name.startsWith("$")) {
                        String lowercaseName = name.toLowerCase(Locale.ENGLISH);
                        if ("$path".equals(lowercaseName)) {
                            if (cookie != null)
                                cookie.setPath(value);
                        } else if ("$domain".equals(lowercaseName)) {
                            if (cookie != null)
                                cookie.setDomain(value);
                        } else if ("$port".equals(lowercaseName)) {
                            if (cookie != null)
                                cookie.setComment("$port=" + value);
                        } else if ("$version".equals(lowercaseName)) {
                            version = Integer.parseInt(value);
                        }
                    } else {
                        cookie = new Cookie(name, value);
                        if (version > 0) {
                            cookie.setVersion(version);
                        }
                        cookies.add(cookie);
                    }
                } catch (Exception e) {
                    log.error("parse cookie happens error!", e);
                }
                name = null;
                value = null;
            }
        }
        return cookies;
    }

    /**
     * 转换有转移符号的字符串
     *
     * @param word    文字字符串
     * @param lenient 是否天剑斜杠转换
     * @return
     */
    static String unquote(String word, boolean lenient) {
        if (word == null) {
            return null;
        }
        if (word.length() < 2) {
            return word;
        }

        char first = word.charAt(0);
        char last = word.charAt(word.length() - 1);
        if (first != last || (first != '"' && first != '\'')) {
            return word;
        }

        StringBuilder b = new StringBuilder(word.length() - 2);
        boolean escape = false;
        for (int i = 1; i < word.length() - 1; i++) {
            char c = word.charAt(i);
            if (c == '\\') {
                escape = true;
            } else if (escape) {
                escape = false;
                if (lenient && !isEscapingLetter(c)) {
                    b.append('\\');
                }
                b.append(c);
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * 判断是否为需要转义的字符
     * @param c
     * @return
     */
    static boolean isEscapingLetter(char c) {
        return ((c == 'n') || (c == 'r') || (c == 't') || (c == 'f') || (c == 'b') || (c == '\\') || (c == '/') || (c == '"') || (c == 'u'));
    }

    /**
     * 按照RFC2109协议进行Cookie解析
     * @param cookie
     * @return
     */
    static List<Cookie> parseCookieRfc2109(String cookie) {
        return new ArrayList<Cookie>();
    }
}
