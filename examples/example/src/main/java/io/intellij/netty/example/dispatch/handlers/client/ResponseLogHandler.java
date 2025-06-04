package io.intellij.netty.example.dispatch.handlers.client;

import io.intellij.netty.example.dispatch.model.msg.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ResponseLogHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ResponseLogHandler extends SimpleChannelInboundHandler<Response> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        log.info("response|{}", msg);
    }
}
