package com.rnkrsoft.embedded.ulwserver.server.servlet;

import org.junit.Test;

import javax.servlet.http.Cookie;
import java.util.List;

/**
 * Created by rnkrsoft.com on 2019/10/14.
 */
public class EmbeddedCookieProcessorTest {

    @Test
    public void testParse() throws Exception {
        List<Cookie> cookies = EmbeddedCookieProcessor.parse("BAIDUID=3766A84E43BDD4835506A12357E38C3CB:FG=1; BIDUPSID=3766A84E43BD12345506AD557E38C3CB; PSTM=1556163163; BD_UPN=12314753; sug=3; bdime=21110; MCITY=-%3A; H_PS_PSSID=1467_21088_18559_29568_29220_26350; ispeed_lsm=2; sugstore=1; BDSVRTM=0");
        System.out.println(cookies);
    }
}