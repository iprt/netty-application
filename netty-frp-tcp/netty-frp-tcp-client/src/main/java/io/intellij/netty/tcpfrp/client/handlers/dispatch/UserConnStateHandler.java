package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.client.service.DirectServiceHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceChannelHandler;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;

/**
 * UserConnStateHandler
 * <p>
 * 处理 用户连接信息的handler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class UserConnStateHandler extends SimpleChannelInboundHandler<UserConnState> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("UserConnStateHandler channelActive");
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UserConnState userConnState) throws Exception {

        ConnState connState = ConnState.getByName(userConnState.getConState());
        if (connState == null) {
            log.error("Unknown conn state: {}", userConnState.getConState());
            ctx.close();
            return;
        }

        switch (connState) {
            // 新建连接：user ---> frp-server:3306
            case ACCEPT:
                final Channel frpsChannel = ctx.channel();
                Promise<Channel> serviceChannelPromise = ctx.executor().newPromise();
                final String userId = userConnState.getUserId();

                ListeningConfig listeningConfig = userConnState.getListeningConfig();

                serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                    Channel serviceChannel = future.getNow();
                    if (future.isSuccess()) {
                        String serviceId = serviceChannel.id().asLongText();
                        log.info("[ACCEPT] service channel create success, serviceId: {}", serviceId);
                        ChannelFuture channelFuture = ctx.writeAndFlush(ServiceConnState.connSuccess(userId, serviceId));
                        channelFuture.addListener((ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) {
                                ChannelPipeline servicePipeline = serviceChannel.pipeline();
                                servicePipeline.addLast(new ServiceChannelHandler(listeningConfig, userId, serviceId, frpsChannel));
                                // for read
                                servicePipeline.fireChannelActive();
                                ctx.read();
                            } else {
                                ctx.close();
                            }
                        });

                    } else {
                        log.error("service channel create failed");
                        ctx.writeAndFlush(ServiceConnState.connFailure(userId));
                    }

                });

                Bootstrap b = new Bootstrap();
                b.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .option(ChannelOption.AUTO_READ, false)
                        .handler(new DirectServiceHandler(serviceChannelPromise));


                log.info("bootstrap try to connect {}:{}", listeningConfig.getLocalIp(), listeningConfig.getLocalPort());
                b.connect(listeningConfig.getLocalIp(), listeningConfig.getLocalPort())
                        .addListener((ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) {
                                log.info("connect to service success|{}", listeningConfig);
                            } else {
                                log.error("connect to service failed|{}", listeningConfig);
                                // TODO 这个地方和Promise那里是不是有点重复了
                                ctx.writeAndFlush(ServiceConnState.connFailure(userId));
                            }
                        });

                break;
            // 断开连接：user -x-> frp-server:3306
            case BROKEN:
                log.info("user disconnect, userId={}, serviceId={}", userConnState.getUserId(), userConnState.getServiceId());
                ServiceChannelHandler.close(userConnState.getServiceId());
                break;
            default:
                log.error("Unknown conn state: {}", connState);
                ctx.close();
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("UserConnStateHandler channelInactive");
        ServiceChannelHandler.closeAll();
        ctx.close();
    }

}
