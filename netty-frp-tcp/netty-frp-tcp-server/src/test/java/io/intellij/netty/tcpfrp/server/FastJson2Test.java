package io.intellij.netty.tcpfrp.server;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.exchange.serversend.GetUserData;
import org.junit.jupiter.api.Test;

/**
 * FastJson2Test
 *
 * @author tech@intellij.io
 */
public class FastJson2Test {
    @Test
    public void testBytes() {

        GetUserData data = GetUserData.builder().userChannelId("user").serviceChannelId("service").data("Hello,World".getBytes()).build();
        String json = JSON.toJSONString(data);
        System.out.println(json.length());

        GetUserData after = JSON.parseObject(json, GetUserData.class);

        String afterJson = JSON.toJSONString(after);
        System.out.println(afterJson.length());

    }
}
