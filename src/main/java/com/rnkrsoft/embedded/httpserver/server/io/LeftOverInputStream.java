package com.rnkrsoft.embedded.httpserver.server.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

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
            throw new IOException("Stream is closed");
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
            throw new IOException("Stream is closed");
        }
        return read0(b, off, len);
    }

    public boolean drain(long l) throws IOException {
        int bufSize = 2048;
        byte[] db = new byte[bufSize];
        while (l > 0) {
            long len = read0(db, 0, bufSize);
            if (len == -1) {
                eof = true;
                return true;
            } else {
                l = l - len;
            }
        }
        return false;
    }
}