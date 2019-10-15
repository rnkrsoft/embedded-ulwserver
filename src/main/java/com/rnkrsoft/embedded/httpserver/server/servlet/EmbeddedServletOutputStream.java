package com.rnkrsoft.embedded.httpserver.server.servlet;

import com.rnkrsoft.io.buffer.ByteBuf;

import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
public class EmbeddedServletOutputStream extends javax.servlet.ServletOutputStream {
    ByteBuf byteBuf;
    ByteBuf byteBuf0;

    public EmbeddedServletOutputStream(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
        this.byteBuf0 = ByteBuf.allocate(8 * 1024).autoExpand(true);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public void write(int b) throws IOException {
        this.byteBuf.put(b);
    }

    @Override
    public void flush() throws IOException {
        byte[] data = this.byteBuf0.getBytes(byteBuf0.readableLength());
        this.byteBuf.put(data);
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byte[] data = new byte[len];
        System.arraycopy(b, off, data, 0, len);
        this.byteBuf0.put(data);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b == null ? 0 : b.length);
    }
}
