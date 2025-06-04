package io.intellij.netty.example.dispatch.handlers.server;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.example.dispatch.model.DataBody;
import io.intellij.netty.example.dispatch.model.msg.LoginReq;
import io.intellij.netty.example.dispatch.model.msg.LogoutReq;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ServerDataBodyHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ServerDataBodyHandler extends SimpleChannelInboundHandler<DataBody> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBody msg) throws Exception {
        if (msg.getDataType() == 1) {
            LoginReq loginReq = JSON.parseObject(msg.getJson(), LoginReq.class);
            ctx.channel().attr(Attributes.LOGIN_USERNAME).set(loginReq.getUsername());
            ctx.fireChannelRead(loginReq);
        } else if (msg.getDataType() == 2) {
            LogoutReq logoutReq = JSON.parseObject(msg.getJson(), LogoutReq.class);
            ctx.channel().attr(Attributes.LOGIN_USERNAME).set(logoutReq.getUsername());
            ctx.fireChannelRead(logoutReq);
        } else {
            throw new RuntimeException("Unknown data type");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("DataBodyHandler error|{}", cause.getMessage());
        ctx.close();
    }

}
