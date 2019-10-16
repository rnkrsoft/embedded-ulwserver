package com.rnkrsoft.embedded.ulwserver.server.io;


import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 * 原生输入流
 */
public class RawInputStream extends InputStream {
    SocketChannel channel;
    ByteBuffer chanBuf;
    byte[] one;
    private boolean closed = false, eof = false;
    ByteBuffer markBuf;
    boolean marked;
    boolean reset;
    int readLimit;
    final static int BUF_SIZE = 8 * 1024;

    public RawInputStream(SocketChannel channel) throws IOException {
        this.channel = channel;
        chanBuf = ByteBuffer.allocate(BUF_SIZE);
        chanBuf.clear();
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
            throw new IOException("RawInputStream is closed");
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
            chanBuf.clear();
            //设置需要读取的长度为传入的长度
            if (srclen < BUF_SIZE) {
                chanBuf.limit(srclen);
            }
            do {
                willReturn = channel.read(chanBuf);
            } while (willReturn == 0);
            if (willReturn == -1) {
                eof = true;
                return -1;
            }
            chanBuf.flip();
            chanBuf.get(b, off, willReturn);

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

    public synchronized int available() throws IOException {
        if (closed)
            throw new IOException("RawInputStream is closed");

        if (eof)
            return -1;

        if (reset)
            return markBuf.remaining();

        return chanBuf.remaining();
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
        this.readLimit = readlimit;
        markBuf = ByteBuffer.allocate(readlimit);
        marked = true;
        reset = false;
    }

    public synchronized void reset() throws IOException {
        if (closed)
            return;
        if (!marked)
            throw new IOException("RawInputStream is not marked");
        marked = false;
        reset = true;
        markBuf.flip();
    }
}
