package io.intellij.netty.tcpfrp.server;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.exchange.s2c.UserDataPacket;
import org.junit.jupiter.api.Test;

/**
 * FastJson2Test
 *
 * @author tech@intellij.io
 */
public class FastJson2Test {
    @Test
    public void testBytes() {

        UserDataPacket data = UserDataPacket.builder().userChannelId("user").serviceChannelId("service").packet("Hello,World".getBytes()).build();
        String json = JSON.toJSONString(data);
        System.out.println(json.length());

        UserDataPacket after = JSON.parseObject(json, UserDataPacket.class);

        String afterJson = JSON.toJSONString(after);
        System.out.println(afterJson.length());

    }
}
