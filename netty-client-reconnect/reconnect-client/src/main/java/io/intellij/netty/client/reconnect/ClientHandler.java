package io.intellij.netty.client.reconnect;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ClientHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("read  msg|{}", msg);
    }
}
