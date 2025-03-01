package io.intellij.netty.example.replaying;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * JsonDecoder
 *
 * @author tech@intellij.io
 * @since 2025-03-02
 */
public class JsonDecoder extends ByteToMessageDecoder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int i = in.readableBytes();
        byte[] bytes = new byte[i];
        in.readBytes(bytes);
        String json = new String(bytes);

        try {
            MAPPER.readTree(json);
            out.add(JsonMsg.builder().valid(true).content(json).build());
        } catch (Exception e) {
            out.add(JsonMsg.builder().valid(false).content(json).build());
        }

    }

}
