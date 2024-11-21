package io.intellij.netty.example.dispatch.handlers.server;

import io.intellij.netty.example.dispatch.model.HeartBeat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * HeartBeatHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ServerHeartBeatHandler extends SimpleChannelInboundHandler<HeartBeat> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HeartBeat msg) throws Exception {
        log.info("receive client heart beat|{}", msg);
        HeartBeat response = HeartBeat.builder()
                .time(new Date())
                .id(msg.getId())
                .seq(msg.getSeq() + 1)
                .build();
        log.info("send server heart beat|{}", response);
        ctx.writeAndFlush(response);
    }

}
