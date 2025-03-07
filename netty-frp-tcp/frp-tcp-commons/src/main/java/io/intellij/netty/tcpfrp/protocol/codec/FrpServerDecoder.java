package io.intellij.netty.tcpfrp.protocol.codec;

import com.alibaba.fastjson2.JSONObject;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.tcpfrp.protocol.channel.DispatchIdUtils;
import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_BASIC_MSG;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_DATA_PACKET;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_LENGTH;
import static io.intellij.netty.tcpfrp.protocol.FrpBasicMsg.State.READ_TYPE;
import static io.intellij.netty.tcpfrp.protocol.FrpMsgType.DATA_PACKET;

/**
 * FrpServerDecoder
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class FrpServerDecoder extends ReplayingDecoder<FrpBasicMsg.State> {
    public FrpServerDecoder() {
        super(READ_TYPE);
    }

    private FrpMsgType type;
    private int length;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        FrpBasicMsg.State state = state();
        switch (state) {
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
                    // 当是数据包时，结束本次 decode 调用，等待下一次数据到来，从正确的状态重新进入
                    return;
                } else {
                    checkpoint(READ_BASIC_MSG);
                }
            case READ_BASIC_MSG:
                byte[] content = new byte[length];
                in.readBytes(content);
                String json = new String(content);
                switch (type) {
                    case AUTH_REQUEST:
                        AuthRequest authRequest = JSONObject.parseObject(json, AuthRequest.class);
                        if (authRequest == null) {
                            throw new IllegalStateException("auth request is null");
                        }
                        out.add(authRequest);
                        break;
                    case LISTENING_REQUEST:
                        ListeningRequest listeningRequest = JSONObject.parseObject(json, ListeningRequest.class);
                        if (listeningRequest == null) {
                            throw new IllegalStateException("listening request is null");
                        }
                        out.add(listeningRequest);
                        break;
                    case SERVICE_CONN_STATE:
                        ServiceConnState serviceConnState = JSONObject.parseObject(json, ServiceConnState.class);
                        if (serviceConnState == null) {
                            throw new IllegalStateException("service conn state is null");
                        }
                        out.add(serviceConnState);
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

                out.add(DataPacket.createAndRetain(dispatchId, in.readSlice(leftLen)));

                checkpoint(READ_TYPE);
                break;
            default:
                throw new IllegalStateException("无效的状态: " + state());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("localAddress={}|remoteAddress={}", ctx.channel().localAddress(), ctx.channel().remoteAddress(), cause);
        ctx.close();
    }

}
