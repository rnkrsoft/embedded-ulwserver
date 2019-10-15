package com.rnkrsoft.embedded.boot;

import com.rnkrsoft.config.AbstractConfigProvider;
import com.rnkrsoft.embedded.boot.annotation.EmbeddedBootApplication;
import com.rnkrsoft.message.MessageFormatter;
import com.rnkrsoft.utils.ValueUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 嵌入式注解配置提供者
 */
@Slf4j
class EmbeddedAnnotationConfigProvider extends AbstractConfigProvider implements Runnable {
    final Properties properties = new Properties();
    String name = "HttpServer";
    /**
     * 嵌入式注解应用
     */
    EmbeddedBootApplication embeddedBootApplicationAnnotation;
    /**
     * 最近一次更新时间戳
     */
    long lastModified = 0L;
    /**
     * 配置文件
     */
    File file;
    /**
     * 热加载properties文件后台线程
     */
    final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
    /**
     * 是否将注解提取的配置信息存储为properties文件
     */
    boolean generateProperties;

    public EmbeddedAnnotationConfigProvider(EmbeddedBootApplication embeddedBootApplicationAnnotation, boolean generateProperties) {
        this.embeddedBootApplicationAnnotation = embeddedBootApplicationAnnotation;
        this.generateProperties = generateProperties;
    }

    void extractAnnotation() {
        properties.setProperty("server.http.hostName", embeddedBootApplicationAnnotation.hostName().isEmpty() ? "localhost" : embeddedBootApplicationAnnotation.hostName());
        properties.setProperty("server.http.port", Integer.toString(embeddedBootApplicationAnnotation.port()));
        properties.setProperty("server.http.maxThreads", Integer.toString(embeddedBootApplicationAnnotation.maxThreads()));
        properties.setProperty("server.http.maxConnections", Integer.toString(embeddedBootApplicationAnnotation.maxConnections()));
        properties.setProperty("server.http.connectionTimeout", Integer.toString(embeddedBootApplicationAnnotation.connectionTimeoutSecond()));
        properties.setProperty("server.http.asyncTimeout", Integer.toString(embeddedBootApplicationAnnotation.asyncTimeoutSecond()));
        properties.setProperty("server.http.uriEncoding", embeddedBootApplicationAnnotation.uriEncoding());
        properties.setProperty("server.http.useBodyEncodingForURI", Boolean.toString(embeddedBootApplicationAnnotation.useBodyEncodingForURI()));
        properties.setProperty("server.http.runtimeDir", embeddedBootApplicationAnnotation.runtimeDir());
        properties.setProperty("server.http.contextPath", embeddedBootApplicationAnnotation.contextPath());
        properties.setProperty("server.http.protocol", embeddedBootApplicationAnnotation.protocol());
        properties.setProperty("file.encoding", embeddedBootApplicationAnnotation.fileEncoding());
    }
    @Override
    public void init(String configDir, int reloadInterval) {
        this.file = new File(configDir, this.name + ".properties");
        init(reloadInterval);
    }

    @Override
    public void init(int reloadInterval) {
        reload();
        scheduledThreadPool.scheduleWithFixedDelay(this, reloadInterval, reloadInterval, TimeUnit.SECONDS);
    }

    @Override
    public void reload() {
        if (this.file == null) {
            System.err.println("file is null");
            return;
        }
        if (generateProperties) {
            return;
        }
        if (this.file.exists()) {
            String fileName = null;
            try {
                fileName = this.file.getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.warn(MessageFormatter.format("use config file startup '{}'", fileName));
            properties.clear();
            InputStream is = null;
            try {
                is = new FileInputStream(this.file);
                properties.load(is);
            } catch (Exception e) {
                ;
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        ;
                    }
                }
            }
        } else {
            log.warn(MessageFormatter.format("use @EmbeddedBootApplication"));
            //从注解提取配置信息
            extractAnnotation();
        }
    }

    @Override
    public void save() {
        if (generateProperties) {
            extractAnnotation();
        }
        FileOutputStream fos = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fos = new FileOutputStream(file);
            properties.store(fos, name);
        } catch (Exception e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    ;
                }
            }
        }
    }

    @Override
    public void param(String name, String value) {

    }

    @Override
    public <T> T getParam(String name, Class<T> clazz) {
        String value = properties.getProperty(name);
        return ValueUtils.convert(value, clazz);
    }

    @Override
    public void run() {
        if (file.lastModified() > lastModified) {
            reload();
        }
    }
}
