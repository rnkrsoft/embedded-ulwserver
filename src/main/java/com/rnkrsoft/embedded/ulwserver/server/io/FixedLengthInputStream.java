package com.rnkrsoft.embedded.ulwserver.server.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rnkrsoft.com on 2019/10/12.
 * 根据指定的长度进行读取，要注意的是如果流中没有这么长的数据，则会造成阻塞
 */
public class FixedLengthInputStream extends LeftOverInputStream {
    long remaining;
    EndStreamCallback endStreamCallback;

    public FixedLengthInputStream(InputStream is, long len, EndStreamCallback endStreamCallback) {
        super(is);
        this.remaining = len;
        this.endStreamCallback = endStreamCallback;
    }

    protected int read0(byte[] b, int off, int len) throws IOException {
        eof = (remaining == 0L);
        if (eof) {
            return -1;
        }
        if (len > remaining) {
            len = (int) remaining;
        }
        int n = in.read(b, off, len);
        if (n > -1) {
            remaining -= n;
            //如果到流的末端，则调用流末端回调函数
            if (remaining == 0 && endStreamCallback != null) {
                endStreamCallback.handle();
            }
        }
        return n;
    }

    public int available() throws IOException {
        if (eof) {
            return 0;
        }
        int n = in.available();
        if(n < remaining){
            return n;
        }else{
            return (int) remaining;
        }
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int l) {
    }

    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
}
