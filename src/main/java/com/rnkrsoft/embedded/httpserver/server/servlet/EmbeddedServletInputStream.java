package com.rnkrsoft.embedded.httpserver.server.servlet;

import com.rnkrsoft.io.buffer.ByteBuf;
import lombok.Getter;

import javax.servlet.ReadListener;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/10/10.
 */
@Getter
public class EmbeddedServletInputStream  extends javax.servlet.ServletInputStream {
    ByteBuf byteBuf;

    public EmbeddedServletInputStream(ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public boolean isReady() {
        return byteBuf.readableLength() > 0;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    @Override
    public int read() throws IOException {
        return byteBuf.getInt();
    }
}
