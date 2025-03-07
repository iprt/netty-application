package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.client.handlers.ServiceChannelManager;
import io.intellij.netty.tcpfrp.client.handlers.initial.ListeningResponseHandler;
import io.intellij.netty.tcpfrp.client.service.DirectServiceHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceChannelHandler;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Triggered from {@link ListeningResponseHandler}
     */
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.info("channelActive: user conn state handler");
        // must but just once
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext frpCtx, @NotNull UserConnState userConnState) throws Exception {
        ConnState connState = ConnState.getByName(userConnState.getConState());
        if (connState == null) {
            log.error("Unknown conn state: {}", userConnState.getConState());
            frpCtx.close();
            return;
        }

        switch (connState) {
            // accept connection ：user ---> frp-server:3306
            case ACCEPT:
                final Channel frpsChannel = frpCtx.channel();
                Promise<Channel> serviceChannelPromise = frpCtx.executor().newPromise();
                final String userId = userConnState.getUserId();

                ListeningConfig config = userConnState.getListeningConfig();
                log.info("[ACCEPT] 接收到用户连接 |userId={}|config={}", userId, config);
                serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                    Channel serviceChannel = future.getNow();
                    if (future.isSuccess()) {
                        String serviceId = serviceChannel.id().asLongText();
                        log.info("[ACCEPT] 接收到用户连接后，服务连接创建成功|userId={}|serviceId={}", userId, serviceId);
                        ChannelPipeline servicePipeline = serviceChannel.pipeline();
                        servicePipeline.addLast(new ServiceChannelHandler(config, userId, serviceId, frpsChannel));
                        // channelActive and Read
                        servicePipeline.fireChannelActive();
                    } else {
                        log.warn("[ACCEPT] 接收到用户连接后，服务连接创建失败|userId={}", userId);
                        frpCtx.writeAndFlush(ServiceConnState.connFailure(userId));
                    }

                });

                Bootstrap b = new Bootstrap();
                b.group(frpCtx.channel().eventLoop())
                        .channel(frpCtx.channel().getClass())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .option(ChannelOption.AUTO_READ, false)
                        .handler(new DirectServiceHandler(serviceChannelPromise));
                b.connect(config.getLocalIp(), config.getLocalPort())
                        .addListener((ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) {
                                String serviceId = cf.channel().id().asLongText();
                                frpCtx.writeAndFlush(ServiceConnState.connSuccess(userId, serviceId))
                                        .addListener((ChannelFutureListener) f2 -> {
                                            if (f2.isSuccess()) {
                                                frpCtx.read();
                                            }
                                        });
                            } else {
                                log.warn("[ACCEPT] 接收到用户连接后，服务连接创建失败|config={}", config);
                            }
                        });
                break;
            // broken connection：user -x-> frp-server:3306
            case BROKEN:
                log.warn("[BROKEN] 接收到用户断开连接|userId={}|serviceId={}|config={}", userConnState.getUserId(), userConnState.getServiceId(), userConnState.getListeningConfig());
                frpCtx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                ServiceChannelManager.getInstance().release(userConnState.getServiceId());
                            }
                        },
                        (ChannelFutureListener) channelFuture -> {
                            if (channelFuture.isSuccess()) {
                                // must
                                frpCtx.read();
                            }
                        }
                );
                break;
            default:
                log.error("Unknown conn state: {}", connState);
                frpCtx.close();
        }

    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.warn("与 frp-server 断开连接，关闭所有服务连接");
        ServiceChannelManager.getInstance().releaseAll();
        ctx.close();
    }

}
