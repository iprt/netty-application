package io.intellij.netty.example.dispatch.handlers.server;

import io.intellij.netty.example.dispatch.model.msg.LogoutReq;
import io.intellij.netty.example.dispatch.model.msg.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * LogoutHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class LogoutHandler extends SimpleChannelInboundHandler<LogoutReq> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LogoutReq msg) throws Exception {
        String attrLoginUsername = ctx.channel().attr(Attributes.LOGIN_USERNAME).get();
        log.info("logout|attrLoginUsername={}|{}", attrLoginUsername, msg);
        log.info("logout business logic ...");
        ctx.writeAndFlush(Response.create(200, "logout success"));
    }
}
