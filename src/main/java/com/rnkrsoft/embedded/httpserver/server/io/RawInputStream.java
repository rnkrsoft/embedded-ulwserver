package com.rnkrsoft.embedded.httpserver.server.io;


import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class RawInputStream extends InputStream {
    SocketChannel channel;
    ByteBuffer chanbuf;
    byte[] one;
    private boolean closed = false, eof = false;
    ByteBuffer markBuf; /* reads may be satisfied from this buffer */
    boolean marked;
    boolean reset;
    int readlimit;
    static long readTimeout;
    final static int BUF_SIZE = 8 * 1024;

    public RawInputStream(SocketChannel channel) throws IOException {
        this.channel = channel;
        chanbuf = ByteBuffer.allocate(BUF_SIZE);
        chanbuf.clear();
        one = new byte[1];
        closed = marked = reset = false;
    }

    public synchronized int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public synchronized int read() throws IOException {
        int result = read(one, 0, 1);
        if (result == 1) {
            return one[0] & 0xFF;
        } else {
            return -1;
        }
    }

    public synchronized int read(byte[] b, int off, int srclen) throws IOException {

        int canReturn, willReturn;

        if (closed) {
            throw new IOException("Stream closed");
        }
        if (eof) {
            return -1;
        }

        if (off < 0 || srclen < 0 || srclen > (b.length - off)) {
            throw new IndexOutOfBoundsException();
        }

        if (reset) { /* satisfy from markBuf */
            canReturn = markBuf.remaining();
            willReturn = canReturn > srclen ? srclen : canReturn;
            markBuf.get(b, off, willReturn);
            if (canReturn == willReturn) {
                reset = false;
            }
        } else { /* satisfy from channel */
            chanbuf.clear();
            //设置需要读取的长度为传入的长度
            if (srclen < BUF_SIZE) {
                chanbuf.limit(srclen);
            }
            do {
                willReturn = channel.read(chanbuf);
            } while (willReturn == 0);
            if (willReturn == -1) {
                eof = true;
                return -1;
            }
            chanbuf.flip();
            chanbuf.get(b, off, willReturn);

            if (marked) { /* copy into markBuf */
                try {
                    markBuf.put(b, off, willReturn);
                } catch (BufferOverflowException e) {
                    marked = false;
                }
            }
        }
        return willReturn;
    }

    public boolean markSupported() {
        return true;
    }

    /* Does not query the OS socket */
    public synchronized int available() throws IOException {
        if (closed)
            throw new IOException("Stream is closed");

        if (eof)
            return -1;

        if (reset)
            return markBuf.remaining();

        return chanbuf.remaining();
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        channel.close();
        closed = true;
    }

    public synchronized void mark(int readlimit) {
        if (closed)
            return;
        this.readlimit = readlimit;
        markBuf = ByteBuffer.allocate(readlimit);
        marked = true;
        reset = false;
    }

    public synchronized void reset() throws IOException {
        if (closed)
            return;
        if (!marked)
            throw new IOException("Stream not marked");
        marked = false;
        reset = true;
        markBuf.flip();
    }
}
