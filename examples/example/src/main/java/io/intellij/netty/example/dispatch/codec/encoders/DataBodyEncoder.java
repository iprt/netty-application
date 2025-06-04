package io.intellij.netty.example.dispatch.codec.encoders;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.example.dispatch.model.DataBody;
import io.intellij.netty.example.dispatch.protocol.ProtocolMsgType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * DataBodyEncoder
 *
 * @author tech@intellij.io
 */
@ChannelHandler.Sharable
public class DataBodyEncoder extends MessageToByteEncoder<DataBody> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, DataBody dataBody, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(ProtocolMsgType.DATA.getType());

        String jsonString = JSON.toJSONString(dataBody);
        byte[] bytes = jsonString.getBytes();

        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
