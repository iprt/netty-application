package io.intellij.netty.tcpfrp.protocol.codec.decoder;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.client.Ping;
import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_BASIC_MSG;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_DISPATCH_PACKET;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_LENGTH;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_TYPE;
import static io.intellij.netty.tcpfrp.protocol.FrpMsgType.DATA_PACKET;
import static io.intellij.netty.tcpfrp.protocol.codec.decoder.FrpClientDecoder.readDispatchPacket;

/**
 * FrpServerDecoder
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
final class FrpServerDecoder extends FrpDecoder {

    private FrpMsgType type;
    private int length;

    public FrpServerDecoder() {
        super(READ_TYPE);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        FrpBasicMsg.State state = state();
        switch (state) {
            case READ_TYPE:
                type = FrpMsgType.getByType(in.readByte());
                if (type == null) {
                    throw new IllegalStateException("无效的消息类型");
                }
                if (DATA_PACKET == type) {
                    checkpoint(READ_DISPATCH_PACKET);
                    return;
                }
                checkpoint(READ_LENGTH);
            case READ_LENGTH:
                length = in.readInt();
                if (length <= 0) {
                    throw new IllegalStateException("无效的消息长度");
                }
                checkpoint(READ_BASIC_MSG);
            case READ_BASIC_MSG:
                byte[] content = new byte[length];
                in.readBytes(content);
                String json = new String(content);
                switch (type) {
                    case AUTH_REQUEST:
                        out.add(jsonToObj(json, AuthRequest.class, "auth request parse error"));
                        break;
                    case LISTENING_REQUEST:
                        out.add(jsonToObj(json, ListeningRequest.class, "listening request parse error"));
                        break;
                    case SERVICE_STATE:
                        out.add(jsonToObj(json, ServiceState.class, "service state parse error"));
                        break;
                    case PING:
                        out.add(jsonToObj(json, Ping.class, "ping parse error"));
                        break;
                    default:
                        throw new IllegalStateException("无效的消息类型: " + type);
                }
                checkpoint(READ_TYPE);
                break;
            case READ_DISPATCH_PACKET:
                readDispatchPacket(in, out);
                checkpoint(READ_TYPE);
                break;
            default:
                throw new IllegalStateException("无效的状态: " + state());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception caught|{}", cause.getMessage(), cause);
        ctx.close();
    }

}
