package io.intellij.netty.tcpfrp.protocol.codec;

import com.alibaba.fastjson2.JSONObject;
import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_BASIC_MSG;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_DATA_PACKET;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_LENGTH;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_TYPE;
import static io.intellij.netty.tcpfrp.protocol.FrpMsgType.DATA_PACKET;

/**
 * FrpClientDecoder
 * <p>
 * frp client 可接收的消息
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
final class FrpClientDecoder extends FrpDecoder {
    public FrpClientDecoder() {
        super(READ_TYPE);
    }

    private FrpMsgType type;
    private int length;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_TYPE:
                type = FrpMsgType.getByType(in.readByte());
                if (type == null) {
                    throw new IllegalStateException("无效的消息类型");
                }
                checkpoint(READ_LENGTH);
            case READ_LENGTH:
                length = in.readInt();
                if (length <= 0) {
                    throw new IllegalStateException("无效的消息长度");
                }
                if (type == DATA_PACKET) {
                    checkpoint(READ_DATA_PACKET);
                    return;
                } else {
                    checkpoint(READ_BASIC_MSG);
                }
            case READ_BASIC_MSG:
                byte[] content = new byte[length];
                in.readBytes(content);
                String json = new String(content);
                switch (type) {
                    case AUTH_RESPONSE:
                        AuthResponse authResponse = JSONObject.parseObject(json, AuthResponse.class);
                        if (authResponse == null) {
                            throw new IllegalStateException("auth response is null");
                        }
                        out.add(authResponse);
                        break;
                    case LISTENING_RESPONSE:
                        ListeningResponse listeningResponse = JSONObject.parseObject(json, ListeningResponse.class);
                        if (listeningResponse == null) {
                            throw new IllegalStateException("listening response is null");
                        }
                        out.add(listeningResponse);
                        break;
                    case USER_CONN_STATE:
                        UserState userState = JSONObject.parseObject(json, UserState.class);
                        if (userState == null) {
                            throw new IllegalStateException("user conn state is null");
                        }
                        out.add(userState);
                        break;
                    default:
                        throw new IllegalStateException("无效的消息类型: " + type);
                }
                checkpoint(READ_TYPE);
                break;
            case READ_DATA_PACKET:
                byte[] dispatchIdBytes = new byte[DispatchIdUtils.ID_LENGTH];
                in.readBytes(dispatchIdBytes);

                String dispatchId = new String(dispatchIdBytes);

                // 读取剩余的字节
                int leftLen = length - DispatchIdUtils.ID_LENGTH;
                if (leftLen <= 0) {
                    throw new IllegalStateException("无效的消息长度");
                }

                out.add(DispatchPacket.createAndRetain(dispatchId, in.readSlice(leftLen)));

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
