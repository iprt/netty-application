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
        // 创建一个嵌入式通道并设置两个解码器，用于处理入站消息
        EmbeddedChannel channel = new EmbeddedChannel(new ReadDecoder(), new JsonDecoder());

        // 构造数据并将其写入 ByteBuf，用于模拟网络传输的数据包
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(Map.of("name", "intellij"));
        byte[] bytes = json.getBytes();
        int msgLength = bytes.length;

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(msgLength);
        buf.writeBytes(bytes);

        // 将构造的数据写入通道
        channel.writeInbound(buf);

        // 从通道中读取解码后的消息并打印
        JsonMsg jsonMsg = channel.readInbound();

        // 打印解码后的消息
        System.out.println("jsonMsg = " + jsonMsg);

        // 关闭通道以释放资源
        channel.close();

    }

}
