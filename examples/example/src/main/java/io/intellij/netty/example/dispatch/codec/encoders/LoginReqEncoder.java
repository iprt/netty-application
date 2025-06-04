package io.intellij.netty.example.dispatch.codec.encoders;

import io.intellij.netty.example.dispatch.model.msg.LoginReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * LoginReqEncoder
 *
 * @author tech@intellij.io
 */
public class LoginReqEncoder extends MessageToMessageEncoder<LoginReq> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LoginReq loginReq, List<Object> list) throws Exception {
        list.add(loginReq.toDataBody());
    }
}
