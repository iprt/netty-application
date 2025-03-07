package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.client.handlers.ServiceChannelManager;
import io.intellij.netty.tcpfrp.client.handlers.initial.ListeningResponseHandler;
import io.intellij.netty.tcpfrp.client.service.DirectServiceHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceChannelHandler;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
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
 * ReceiveUserConnStateHandler
 * <p>
 * 处理 用户连接信息的handler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ReceiveUserConnStateHandler extends SimpleChannelInboundHandler<UserConnState> {

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
                final Channel frpChannel = frpCtx.channel();
                Promise<Channel> serviceChannelPromise = frpCtx.executor().newPromise();
                final String dispatchId = userConnState.getDispatchId();

                ListeningConfig config = userConnState.getListeningConfig();
                log.info("[ACCEPT] 接收到用户连接 |dispatchId={}|config={}", dispatchId, config);
                serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                    Channel serviceChannel = future.getNow();
                    if (future.isSuccess()) {
                        log.info("[ACCEPT] 接收到用户连接后，服务连接创建成功|dispatchId={}", dispatchId);
                        ChannelPipeline servicePipeline = serviceChannel.pipeline();
                        servicePipeline.addLast(new ServiceChannelHandler(config, dispatchId, FrpChannel.build(frpChannel)));
                        // channelActive and Read
                        servicePipeline.fireChannelActive();
                    } else {
                        log.warn("[ACCEPT] 接收到用户连接后，服务连接创建失败|dispatchId={}", dispatchId);
                        frpCtx.writeAndFlush(ServiceConnState.connFailure(dispatchId)).addListeners(
                                (ChannelFutureListener) f -> {
                                    if (f.isSuccess()) {
                                        // must
                                        frpCtx.read();
                                    }
                                }
                        );
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
                                frpCtx.writeAndFlush(ServiceConnState.connSuccess(dispatchId))
                                        .addListener((ChannelFutureListener) f2 -> {
                                            if (f2.isSuccess()) {
                                                frpCtx.read();
                                            }
                                        });
                            } else {
                                log.warn("[ACCEPT] 接收到用户连接后，服务连接创建失败|config={}", config);
                                frpCtx.writeAndFlush(ServiceConnState.connFailure(dispatchId)).addListeners(
                                        (ChannelFutureListener) f -> {
                                            if (f.isSuccess()) {
                                                // must
                                                frpCtx.read();
                                            }
                                        }
                                );
                            }
                        });
                break;
            // broken connection：user -x-> frp-server:3306
            case BROKEN:
                log.warn("[BROKEN] 接收到用户断开连接|dispatchId={}|config={}", userConnState.getDispatchId(), userConnState.getListeningConfig());
                frpCtx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                ServiceChannelManager.getInstance().release(userConnState.getDispatchId());
                            }
                        },
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
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
