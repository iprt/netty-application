package io.intellij.netty.example.dispatch.codec;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.example.dispatch.model.DataBody;
import io.intellij.netty.example.dispatch.model.HeartBeat;
import io.intellij.netty.example.dispatch.protocol.ProtocolMsgType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * DispatchDecoder
 *
 * @author tech@intellij.io
 */
@Slf4j
public class DispatchDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }
        int type = in.readInt();
        ProtocolMsgType protocolMsgType = ProtocolMsgType.get(type);
        if (Objects.isNull(protocolMsgType)) {
            throw new IllegalArgumentException("Illegal protocol type");
        }

        if (in.readableBytes() < 4) {
            in.resetReaderIndex();
            return;
        }

        int len = in.readInt();
        if (in.readableBytes() < len) {
            in.resetReaderIndex();
            return;
        }
        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        String msgJson = new String(bytes);

        if (ProtocolMsgType.HEARTBEAT == protocolMsgType) {
            out.add(JSON.parseObject(msgJson, HeartBeat.class));
        }

        if (ProtocolMsgType.DATA == protocolMsgType) {
            out.add(JSON.parseObject(msgJson, DataBody.class));
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("DispatchDecoder error", cause);
        ctx.close();
    }

}
