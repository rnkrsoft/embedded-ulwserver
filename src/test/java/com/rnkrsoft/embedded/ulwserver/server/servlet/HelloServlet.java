package com.rnkrsoft.embedded.ulwserver.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class HelloServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("url:" + req.getRequestURI());
        System.out.println(req.getContextPath());
        //打印结果：/news
        System.out.println(req.getServletPath());
        //打印结果：/main/list.jsp
        System.out.println(req.getRequestURI());
        //打印结果：/news/main/list.jsp
        System.out.println(req.getRealPath("/"));
        //打印结果： F:\Tomcat 6.0\webapps\news\test
        System.out.println("headers:" + Collections.list(req.getHeaderNames()));
        {
            Cookie cookie = new Cookie("cookie1", "test1");
            cookie.setDomain("localhost");
            cookie.setMaxAge(10);
            cookie.setVersion(1);
            resp.addCookie(cookie);
        }
        {
            Cookie cookie = new Cookie("cookie2", "test2");
            cookie.setDomain(".rnkrsoft.com");
            cookie.setMaxAge(30);
            cookie.setVersion(1);
            resp.addCookie(cookie);
        }
        {
            Cookie cookie = new Cookie("cookie3", "test3");
            cookie.setDomain("");
            cookie.setMaxAge(1);
            cookie.setVersion(1);
            resp.addCookie(cookie);
        }
        OutputStream os = resp.getOutputStream();
        String[] name = req.getParameterValues("name");
        if (name != null){
            if ( name.length == 1){
                os.write("hello world,".getBytes());
                os.write(name[0].getBytes());
            }else{
                os.write("hello world,".getBytes());
            }
        }else{
            os.write("hello world".getBytes());
        }
        os.close();
    }
}
