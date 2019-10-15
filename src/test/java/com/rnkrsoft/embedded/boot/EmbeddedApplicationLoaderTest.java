package com.rnkrsoft.embedded.boot;

import com.rnkrsoft.embedded.boot.annotation.EmbeddedBootApplication;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rnkrsoft.com on 2019/10/13.
 */
@EmbeddedBootApplication(
        port = 8090
)
public class EmbeddedApplicationLoaderTest {

    @Test
    public void testRunWith() throws Exception {
        EmbeddedApplicationLoader.runWith(EmbeddedApplicationLoaderTest.class, new String[0]);
    }
}