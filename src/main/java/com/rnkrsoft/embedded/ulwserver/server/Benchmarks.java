package com.rnkrsoft.embedded.ulwserver.server;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 * 性能工具类，正式发布时计时代码将全部移除
 */
public class Benchmarks {
    static long millis = System.currentTimeMillis();
    public static void begin(){
        millis = System.currentTimeMillis();
    }
    public static void end(String message){
        long cost = (System.currentTimeMillis() - millis);
        System.out.println(message + " " + cost + " ms");
    }
}
