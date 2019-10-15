package com.rnkrsoft.embedded.httpserver.server.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * Created by rnkrsoft.com on 2019/10/12.
 * 剩余内容的输入流
 */
abstract class LeftOverInputStream extends FilterInputStream {
    protected boolean closed = false;
    protected boolean eof = false;
    byte[] one = new byte[1];

    public LeftOverInputStream(InputStream src) {
        super(src);
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        if (!eof) {
            //TODO 当关闭时排空最大64KB数据
            eof = drain(64 * 1024);
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isEOF() {
        return eof;
    }

    protected abstract int read0(byte[] b, int off, int len) throws IOException;

    public synchronized int read() throws IOException {
        if (closed) {
            throw new IOException("LeftOverInputStream is closed");
        }
        int c = read0(one, 0, 1);
        if (c == -1 || c == 0) {
            return c;
        } else {
            return one[0] & 0xFF;
        }
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("LeftOverInputStream is closed");
        }
        return read0(b, off, len);
    }

    /**
     * 将指定长度的输入流进行排空
     * @param length 排空长度
     * @return 是否排空成功
     * @throws IOException IO异常
     */
    public boolean drain(long length) throws IOException {
        int bufSize = 2048;
        byte[] db = new byte[bufSize];
        while (length > 0) {
            long len = read0(db, 0, bufSize);
            if (len == -1) {
                eof = true;
                return true;
            } else {
                length = length - len;
            }
        }
        return false;
    }
}