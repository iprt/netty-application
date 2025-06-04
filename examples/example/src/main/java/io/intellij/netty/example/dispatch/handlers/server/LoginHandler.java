package io.intellij.netty.example.dispatch.handlers.server;

import io.intellij.netty.example.dispatch.model.msg.LoginReq;
import io.intellij.netty.example.dispatch.model.msg.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * LoginHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class LoginHandler extends SimpleChannelInboundHandler<LoginReq> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginReq msg) throws Exception {
        String attrLoginUsername = ctx.channel().attr(Attributes.LOGIN_USERNAME).get();
        log.info("login|attrLoginUsername={}|{}", attrLoginUsername, msg);
        log.info("login business logic ...");
        ctx.writeAndFlush(Response.create(200, "login success"));
    }
}
