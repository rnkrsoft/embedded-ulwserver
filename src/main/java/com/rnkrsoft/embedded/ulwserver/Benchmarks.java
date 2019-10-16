package com.rnkrsoft.embedded.ulwserver;

/**
 * Created by rnkrsoft.com on 2019/10/16.
 * 性能工具类
 */
public class Benchmarks {
    static long nanoTime = System.nanoTime();
    public static void beginNanoTime(){
        nanoTime = System.nanoTime();
    }
    public static void endNanoTime(String message){
        long cost = (System.nanoTime() - nanoTime);
        System.out.println(message + " " + cost + " nanoTime");
    }
}
