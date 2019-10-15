package com.rnkrsoft.embedded.boot;

import com.rnkrsoft.io.buffer.ByteBuf;
import com.rnkrsoft.reflection4j.resource.Resource;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * Created by rnkrsoft.com on 2019/9/21.
 */
@Slf4j
public class EmbeddedDeployScriptGenerator {
    /**
     * 生成部署脚本
     * @param basePath 保存目录
     * @param templateName 模板名
     */
    public static void generateDeployScript(String basePath, String templateName){
        try {
            String script = Resource.readFileToString(Resource.CLASS_PATH + "META-INF/embedded/scripts/" + templateName, "UTF-8");
            ByteBuf byteBuf = ByteBuf.allocate(1024).autoExpand(true);
            byteBuf.putUTF8(script);
            byteBuf.write(new File(basePath, templateName).getCanonicalPath());
        }catch (IOException e){
            log.error("generate deploy script file happens error!", e);
        }
    }
}
