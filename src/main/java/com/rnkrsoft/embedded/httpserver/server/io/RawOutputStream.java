package com.rnkrsoft.embedded.httpserver.server.io;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Slf4j
public class RawOutputStream extends java.io.OutputStream {
    SocketChannel socketChannel;
    ByteBuffer buf;
    boolean closed;
    byte[] bytes;

    public RawOutputStream(SocketChannel socketChannel) throws IOException {
        assert socketChannel.isBlocking();
        this.socketChannel = socketChannel;
        closed = false;
        bytes = new byte[1];
        buf = ByteBuffer.allocate(4096);
    }

    public void write(int i) throws IOException {
        bytes[0] = (byte) i;
        write(bytes, 0, 1);
    }

    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    public void write(byte[] bytes, int off, int len) throws IOException {
        int length = len;
        if (closed) {
            throw new IOException("OutputStream is closed");
        }
        int cap = buf.capacity();
        if (cap < len) {
            int diff = len - cap;
            buf = ByteBuffer.allocate(2 * (cap + diff));
        }
        buf.clear();
        buf.put(bytes, off, len);
        buf.flip();
        int n;
        while ((n = socketChannel.write(buf)) < length) {
            length -= n;
            if (length == 0) {
                return;
            }
        }
    }

    public void close() throws IOException {
        if (closed) {
            return;
        }
        if (log.isDebugEnabled()){
            log.debug("RawOutputStream.close, socketChannel.isOpen()={}", socketChannel.isOpen());
        }
        socketChannel.close();
        closed = true;
    }
}
