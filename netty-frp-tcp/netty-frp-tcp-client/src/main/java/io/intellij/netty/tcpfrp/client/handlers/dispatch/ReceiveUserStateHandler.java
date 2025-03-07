package io.intellij.netty.tcpfrp.client.handlers.dispatch;

import io.intellij.netty.tcpfrp.client.handlers.initial.ListeningResponseHandler;
import io.intellij.netty.tcpfrp.client.service.DirectServiceHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceChannelHandler;
import io.intellij.netty.tcpfrp.commons.DispatchManager;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
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

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReceiveUserStateHandler
 * <p>
 * 处理 用户连接信息的handler
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ReceiveUserStateHandler extends SimpleChannelInboundHandler<UserState> {
    private final Map<Integer, ListeningConfig> portToConfig;

    public ReceiveUserStateHandler(Map<String, ListeningConfig> configMap) {
        this.portToConfig = configMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getValue().getRemotePort(),
                        Map.Entry::getValue
                ));
    }

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
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull UserState connState) throws Exception {
        ConnState userState = ConnState.getByName(connState.getStateName());
        if (userState == null) {
            log.error("Unknown conn state: {}", connState.getStateName());
            ctx.close();
            return;
        }

        switch (userState) {
            // accept connection ：user ---> frp-server:3306
            case ACCEPT:
                final Channel frpChannel = ctx.channel();
                Promise<Channel> serviceChannelPromise = ctx.executor().newPromise();
                final String dispatchId = connState.getDispatchId();
                ListeningConfig config = portToConfig.get(connState.getListeningPort());

                log.info("[ACCEPT] 接收到用户连接 |dispatchId={}|name={}", dispatchId, config.getName());
                serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                    Channel serviceChannel = future.getNow();
                    if (future.isSuccess()) {
                        log.info("[ACCEPT] 接收到用户连接后，服务连接创建成功|dispatchId={}|name={}", dispatchId, config.getName());
                        ChannelPipeline servicePipeline = serviceChannel.pipeline();
                        servicePipeline.addLast(new ServiceChannelHandler(config.getName(), dispatchId, FrpChannel.build(frpChannel)));
                        // channelActive and Read
                        servicePipeline.fireChannelActive();
                    } else {
                        log.warn("[ACCEPT] 接收到用户连接后，服务连接创建失败|dispatchId={}", dispatchId);
                        ctx.writeAndFlush(ServiceState.connFailure(dispatchId)).addListeners(
                                (ChannelFutureListener) f -> {
                                    if (f.isSuccess()) {
                                        // must
                                        ctx.read();
                                    }
                                }
                        );
                    }

                });

                Bootstrap b = new Bootstrap();
                b.group(ctx.channel().eventLoop())
                        .channel(ctx.channel().getClass())
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                        .option(ChannelOption.AUTO_READ, false)
                        .handler(new DirectServiceHandler(serviceChannelPromise));

                b.connect(config.getLocalIp(), config.getLocalPort())
                        .addListener((ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) {
                                ctx.writeAndFlush(ServiceState.connSuccess(dispatchId))
                                        .addListener((ChannelFutureListener) f2 -> {
                                            if (f2.isSuccess()) {
                                                ctx.read();
                                            }
                                        });
                            } else {
                                log.warn("[ACCEPT] 接收到用户连接后，服务连接创建失败|name={}", config.getName());
                                ctx.writeAndFlush(ServiceState.connFailure(dispatchId)).addListeners(
                                        (ChannelFutureListener) f -> {
                                            if (f.isSuccess()) {
                                                // must
                                                ctx.read();
                                            }
                                        }
                                );
                            }
                        });
                break;
            case READY:
                log.info("[READY] 接收到用户连接就绪状态，可以读取数据了|dispatchId={}", connState.getDispatchId());
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                DispatchManager.getInstance().initiativeChannelRead(connState.getDispatchId());
                            }
                        }
                );
                break;
            // broken connection：user -x-> frp-server:3306
            case BROKEN:
                log.warn("[BROKEN] 接收到用户断开连接|dispatchId={}", connState.getDispatchId());
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListeners(
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                DispatchManager.getInstance().release(connState.getDispatchId());
                            }
                        },
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                // must
                                ctx.read();
                            }
                        }
                );
                break;
            default:
                log.error("Unknown conn state: {}", userState);
                ctx.close();
        }

    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.warn("disconnected from frp-server,then release all channels and close ctx");
        DispatchManager.getInstance().releaseAll();
        ctx.close();
    }

}
