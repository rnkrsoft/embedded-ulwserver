package com.rnkrsoft.embedded.httpserver.server.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rnkrsoft.com on 2019/10/11.
 */
public class IOUtils {
    public static final int EOF = -1;

    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    public static long copy(final InputStream is, final OutputStream os) throws IOException {
        //写入4K,4K作为1页的空间
        return copy(is, os, new byte[4 * 1024]);
    }
    public static long copy(final InputStream is, final OutputStream os, final byte[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = is.read(buffer))) {
            os.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
