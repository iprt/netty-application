package io.intellij.netty.tcpfrp.protocol.codec;

import com.alibaba.fastjson2.JSONObject;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Ping;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Pong;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_BASIC_MSG;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_DISPATCH_PACKET;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_LENGTH;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_TYPE;
import static io.intellij.netty.tcpfrp.protocol.FrpMsgType.DATA_PACKET;

/**
 * FrpDecoder
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
@Slf4j
final class FrpDecoder extends ReplayingDecoder<FrpBasicMsg.State> {
    enum MODE {
        SERVER, CLIENT
    }

    private final MODE mode;

    private FrpMsgType type;
    private int length;


    FrpDecoder(MODE mode) {
        super(READ_TYPE);
        if (mode == null) {
            throw new IllegalArgumentException("mode is null");
        }
        this.mode = mode;
    }

    private <T> T jsonToObj(String json, Class<T> clazz, String errorMsg) {
        T data = JSONObject.parseObject(json, clazz);
        if (data == null) {
            throw new RuntimeException(errorMsg);
        }
        return data;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_TYPE:
                type = FrpMsgType.getByType(in.readByte());
                if (type == null) {
                    throw new IllegalStateException("无效的消息类型");
                }
                if (DATA_PACKET == type) {
                    checkpoint(READ_DISPATCH_PACKET);
                    break;
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

                switch (mode) {
                    case SERVER:
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
                        break;
                    case CLIENT:
                        switch (type) {
                            case AUTH_RESPONSE:
                                out.add(jsonToObj(json, AuthResponse.class, "auth response parse error"));
                                break;
                            case LISTENING_RESPONSE:
                                out.add(jsonToObj(json, ListeningResponse.class, "listening response parse error"));
                                break;
                            case USER_STATE:
                                out.add(jsonToObj(json, UserState.class, "user state parse error"));
                                break;
                            case PONG:
                                out.add(jsonToObj(json, Pong.class, "pong parse error"));
                                break;
                            default:
                                throw new IllegalStateException("无效的消息类型: " + type);
                        }
                        break;
                }

                checkpoint(READ_TYPE);
                break;
            case READ_DISPATCH_PACKET:
                byte[] dispatchIdBytes = new byte[DispatchIdUtils.ID_LENGTH];
                in.readBytes(dispatchIdBytes);
                String dispatchId = new String(dispatchIdBytes);
                int packetLen = in.readInt();
                if (packetLen <= 0) {
                    throw new IllegalStateException("无效的DispatchPacket消息长度");
                }
                out.add(DispatchPacket.createAndRetain(dispatchId, in.readSlice(packetLen)));
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

    static ChannelInboundHandler serverDecoder() {
        return new FrpDecoder(MODE.SERVER);
    }

}
