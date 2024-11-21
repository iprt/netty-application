package io.intellij.netty.example.dispatch.codec.encoders;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.example.dispatch.model.HeartBeat;
import io.intellij.netty.example.dispatch.protocol.ProtocolMsgType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * HeartBeatEncoder
 *
 * @author tech@intellij.io
 */
@ChannelHandler.Sharable
public class HeartBeatEncoder extends MessageToByteEncoder<HeartBeat> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, HeartBeat heartBeat, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(ProtocolMsgType.HEARTBEAT.getType());

        String jsonString = JSON.toJSONString(heartBeat);
        byte[] bytes = jsonString.getBytes();

        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }
}
