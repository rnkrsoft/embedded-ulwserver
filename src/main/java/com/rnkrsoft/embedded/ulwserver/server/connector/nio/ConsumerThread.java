package com.rnkrsoft.embedded.ulwserver.server.connector.nio;

import com.rnkrsoft.embedded.ulwserver.HttpConnection;
import com.rnkrsoft.embedded.ulwserver.HttpProtocol;
import com.rnkrsoft.embedded.ulwserver.server.protocol.http11.Http11Protocol;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * 处理任务，主要完成HTTP协议的解析，属于业务线程
 */
class ConsumerThread implements Runnable {
    HttpConnection httpConnection;

    static final ThreadLocal<HttpProtocol> protocols = new ThreadLocal<HttpProtocol>();

    public ConsumerThread(HttpConnection httpConnection) {
        this.httpConnection = httpConnection;
    }

    @Override
    public void run() {
        //获取连接器对象
        HttpProtocol protocol = protocols.get();
        if (protocol == null){
            protocol = new Http11Protocol();
            protocols.set(protocol);
        }else{
            //清理连接器成员变量和状态
            protocol.recycle();
        }
        try {
            //连接器处理连接对象
            protocol.handle(httpConnection);
        } catch (IOException e) {
            e.printStackTrace();
        }catch (RuntimeException e){
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            this.httpConnection.close();
        }
    }
}