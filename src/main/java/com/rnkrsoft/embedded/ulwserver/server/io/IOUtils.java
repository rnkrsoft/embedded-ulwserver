package com.rnkrsoft.embedded.ulwserver.server.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rnkrsoft.com on 2019/10/11.
 * IO工具类
 */
public class IOUtils {
    public static final int EOF = -1;

    /**
     * 静默关闭可关闭对象
     * @param closeable 可关闭对象
     */
    public static void closeQuietly(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            // ignore
        }
    }

    /**
     * 将输入流中的所有内容写入输出流中，使用4K缓冲区
     *
     * @param is 输入流
     * @param os 输出流
     * @return 复制字节数
     * @throws IOException IO异常
     */
    public static long copy(final InputStream is, final OutputStream os) throws IOException {
        //写入4K,4K作为1页的空间
        return copy(is, os, new byte[4 * 1024]);
    }

    /**
     * 将输入流的内容写入输出流中
     *
     * @param is     输入流
     * @param os     输出流
     * @param buffer 缓冲区
     * @return 复制字节数
     * @throws IOException IO异常
     */
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
