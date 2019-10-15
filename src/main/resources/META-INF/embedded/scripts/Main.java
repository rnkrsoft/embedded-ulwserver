package com.xxx.main;

import com.rnkrsoft.embedded.boot.EmbeddedApplicationLoader;
import com.rnkrsoft.embedded.boot.annotation.EmbeddedBootApplication;

@EmbeddedBootApplication(
        runtimeDir = "./work",
        hostName = "localhost",
        port = 8080
)
public class Main {

    public static void main(String[] args) {
        EmbeddedApplicationLoader.runWith(Main.class, args);
    }
}
