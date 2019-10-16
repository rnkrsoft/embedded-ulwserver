package com.rnkrsoft.embedded.boot;

import com.rnkrsoft.config.ConfigProvider;
import com.rnkrsoft.embedded.boot.annotation.EmbeddedBootApplication;
import com.rnkrsoft.embedded.boot.annotation.EmbeddedRemoteConfigure;
import com.rnkrsoft.embedded.ulwserver.HttpServerStartup;
import com.rnkrsoft.framework.config.v1.RuntimeMode;
import com.rnkrsoft.logtrace4j.ErrorContextFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class EmbeddedApplicationLoader {
    static final String MESSAGE;
    static ConfigProvider CONFIG = null;

    static {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Embedded Tomcat Boot Application").append("\n");
        buffer.append(" -generate(g): generate tomcat.properties file.").append("\n");
        buffer.append(" -script(s): generate deploy script file.").append("\n");
        buffer.append(" -pom(p): generate pom file.").append("\n");
        buffer.append(" -verbose(v): y/n verbose mode.").append("\n");
        MESSAGE = buffer.toString();
    }

    public final static void runWith(Class bootLoaderClass, String... args) {
        //生成容器配置文件
        boolean generateProperties = false;
        //生成部署脚本文件
        boolean generateDeployScript = false;
        //生成pom文件
        boolean generatePom = false;
        boolean verbose = false;
        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            if (name.equals("-generate") || name.equals("-g")) {
                generateProperties = true;
                break;
            } else if (name.equals("-script") || name.equals("-s")) {
                generateProperties = true;
                break;
            } else if (name.equals("-pom") || name.equals("-p")) {
                generatePom = true;
                break;
            } else if (name.equals("-verbose") || name.equals("-v")) {
                verbose = true;
                if (verbose) {
                    System.out.println("开始唐生模式");
                }
                break;
            } else if (name.equals("-help") || name.equals("-h")) {
                System.out.println(MESSAGE);
                System.exit(0);
                return;
            } else {
                System.out.println(MESSAGE);
                System.exit(0);
                return;
            }
        }

        EmbeddedBootApplication embeddedBootApplicationAnnotation = (EmbeddedBootApplication) bootLoaderClass.getAnnotation(EmbeddedBootApplication.class);
        if (embeddedBootApplicationAnnotation == null) {
            throw ErrorContextFactory.instance()
                    .message("Class {} unmarked @EmbeddedBootApplication annotation!", bootLoaderClass)
                    .solution("Class {} unmarked  @EmbeddedBootApplication annotation solute the problem！", bootLoaderClass)
                    .runtimeException();
        }
        Map<String, String> envs = System.getenv();
        List<String> keys = new ArrayList(envs.keySet());
        Collections.sort(keys);
        EmbeddedRemoteConfigure remoteConfigure = embeddedBootApplicationAnnotation.remoteConfigure();
        String configHost = remoteConfigure.host();
        Integer configPort = remoteConfigure.port();
        String configEnv = remoteConfigure.env();
        RuntimeMode runtimeMode = remoteConfigure.runtimeMode();
        String configWorkHome = System.getProperty("user.dir") + "/work";
        String configSecurityKey = remoteConfigure.securityKey();
        if (envs.containsKey("CONFIG_HOST")) {
            configHost = envs.get("CONFIG_HOST");
        }
        if (envs.containsKey("CONFIG_PORT")) {
            configPort = Integer.valueOf(envs.get("CONFIG_PORT"));
        }
        if (envs.containsKey("CONFIG_ENV")) {
            configEnv = envs.get("CONFIG_ENV");
        }
        if (envs.containsKey("CONFIG_RUNTIME_MODE")) {
            runtimeMode = RuntimeMode.valueOfCode(envs.get("CONFIG_RUNTIME_MODE"));
        }
        if (envs.containsKey("CONFIG_WORK_HOME")) {
            configWorkHome = envs.get("CONFIG_WORK_HOME");
        }
        if (envs.containsKey("CONFIG_SECURITY_KEY")) {
            configSecurityKey = envs.get("CONFIG_SECURITY_KEY");
        }
        if (runtimeMode == RuntimeMode.REMOTE || runtimeMode == RuntimeMode.AUTO) {
            CONFIG = new EmbeddedRemoteConfigProvider(configHost, configPort, remoteConfigure.groupId(), remoteConfigure.artifactId(), remoteConfigure.version(), configEnv, configSecurityKey, runtimeMode, verbose);
            try {
                CONFIG.init(configWorkHome, embeddedBootApplicationAnnotation.reloadConfigSecond());
            } catch (RuntimeException e) {
                if (runtimeMode == RuntimeMode.AUTO) {
                    CONFIG = new EmbeddedAnnotationConfigProvider(embeddedBootApplicationAnnotation, generateProperties);
                    CONFIG.init(configWorkHome, embeddedBootApplicationAnnotation.reloadConfigSecond());
                    log.error("fetch remote config happens error,fallback:LOCAL", e);
                } else {
                    log.error("fetch remote config happens error", e);
                    throw e;
                }
            }
        } else {
            CONFIG = new EmbeddedAnnotationConfigProvider(embeddedBootApplicationAnnotation, generateProperties);
            CONFIG.init(configWorkHome, embeddedBootApplicationAnnotation.reloadConfigSecond());
        }
        System.setProperty("file.encoding", CONFIG.getString("fileEncoding", "UTF-8"));
        /**
         * 生成properties文件
         */
        if (generateProperties) {
            System.out.println("[begin]generate tomcat.properties file");
            CONFIG.save();
            System.out.println("[end]generate tomcat.properties file");
            System.exit(0);
            return;
        }
        if (generateDeployScript) {
            System.out.println("[begin]generate deploy script file");
            EmbeddedDeployScriptGenerator.generateDeployScript(".", "log4j2.xml");
            EmbeddedDeployScriptGenerator.generateDeployScript(".", "README.md");
            EmbeddedDeployScriptGenerator.generateDeployScript(".", "startup.bat");
            EmbeddedDeployScriptGenerator.generateDeployScript(".", "startup.sh");
            System.out.println("[end]generate deploy script file");
            System.exit(0);
            return;
        }
        if (generatePom) {
            System.out.println("[begin]generate pom file");
            EmbeddedDeployScriptGenerator.generateDeployScript("./demo-project", "pom.xml");
            EmbeddedDeployScriptGenerator.generateDeployScript("./demo-project/src/main/java/com/xxx/main", "Main.java");
            EmbeddedDeployScriptGenerator.generateDeployScript("./demo-project", "embedded-tomcat.md");
            System.out.println("[end]generate pom file");
            System.exit(0);
            return;
        }
        try {
            HttpServerStartup.main(CONFIG);
        } catch (Exception e) {
            log.error("Embedded Tomcat startup happens error!", e);
        }
    }

    public static ConfigProvider getConfigProvider() {
        if (CONFIG == null) {
            throw ErrorContextFactory.instance()
                    .message("Config Provider is not already!")
                    .solution("please use Skeleton4jApplicationLoader.runWith(bootLoaderClass, args);")
                    .runtimeException();
        } else {
            return CONFIG;
        }
    }
}
