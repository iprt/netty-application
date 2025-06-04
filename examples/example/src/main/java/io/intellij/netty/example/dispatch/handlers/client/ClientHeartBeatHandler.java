package io.intellij.netty.example.dispatch.handlers.client;

import io.intellij.netty.example.dispatch.model.HeartBeat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ClientHeartBeatHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ClientHeartBeatHandler extends SimpleChannelInboundHandler<HeartBeat> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartBeat msg) throws Exception {
        log.info("receive server heart beat|{}", msg);
    }

}
