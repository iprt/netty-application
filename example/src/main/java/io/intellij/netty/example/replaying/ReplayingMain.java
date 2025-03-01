package io.intellij.netty.example.replaying;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.SneakyThrows;

import java.util.Map;

/**
 * ReplayingMain
 *
 * @author tech@intellij.io
 * @since 2025-03-02
 */
public class ReplayingMain {

    @SneakyThrows
    public static void main(String[] args) {
        EmbeddedChannel channel = new EmbeddedChannel(
                new ReadDecoder(),
                new JsonDecoder()
        );

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Map.of("name", "intellij"));
        byte[] bytes = json.getBytes();
        int msgLength = bytes.length;

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(msgLength);
        buf.writeBytes(bytes);

        // 将构造的数据写入通道
        channel.writeInbound(buf);

        JsonMsg jsonMsg = channel.readInbound();
        System.out.println("解码后的消息" + jsonMsg);

        channel.close();
    }

}
