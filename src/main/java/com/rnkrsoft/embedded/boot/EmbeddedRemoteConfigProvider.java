package com.rnkrsoft.embedded.boot;

import com.rnkrsoft.config.AbstractConfigProvider;
import com.rnkrsoft.framework.config.client.ConfigClient;
import com.rnkrsoft.framework.config.client.ConfigClientSetting;
import com.rnkrsoft.framework.config.utils.MachineUtils;
import com.rnkrsoft.framework.config.v1.ConnectorType;
import com.rnkrsoft.framework.config.v1.RuntimeMode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/5/24.
 * 嵌入式远程配置中心提供者
 */
@Slf4j
class EmbeddedRemoteConfigProvider extends AbstractConfigProvider {
    /**
     * 配置中心
     */
    ConfigClient configClient;
    String host;
    int port;
    String groupId;
    String artifactId;
    String version;
    String env;
    String securityKey;
    RuntimeMode runtimeMode;
    boolean verbose;
    String name = "tomcat";
    File file;

    public EmbeddedRemoteConfigProvider(String host, int port, String groupId, String artifactId, String version, String env, String securityKey, RuntimeMode runtimeMode, boolean verbose) {
        this.configClient = ConfigClient.getInstance();
        this.host = host;
        this.port = port;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.env = env;
        this.securityKey = securityKey;
        this.runtimeMode = runtimeMode;
        this.verbose = verbose;
    }

    @Override
    public void init(String configDir, int reloadInterval) {
        this.file = new File(configDir, this.name + ".properties");
        ConfigClientSetting setting = ConfigClientSetting.builder()
                .host(host)
                .port(port)
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .env(env)
                .machine(MachineUtils.getHostName())
                .connectorType(ConnectorType.HTTP)
                .fileEncoding("UTF-8")
                .securityKey(securityKey)
                .runtimeMode(runtimeMode)
                .fetchIntervalSeconds(reloadInterval)
                .workHome(configDir)
                .printLog(verbose)
                .build();
        this.configClient.init(setting);
    }

    @Override
    public void init(int reloadInterval) {
        init(".", reloadInterval);
    }

    @Override
    public void reload() {
        this.configClient.fetch();
    }

    @Override
    public void save() {
        FileOutputStream fos = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            fos = new FileOutputStream(file);
            this.configClient.getProperties().store(fos, name);
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
    public <T> T getParam(String name, Class<T> type) {
        return this.configClient.getProperty(name, null, type);
    }
}
