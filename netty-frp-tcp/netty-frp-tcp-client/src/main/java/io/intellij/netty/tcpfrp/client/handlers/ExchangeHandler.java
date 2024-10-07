package io.intellij.netty.tcpfrp.client.handlers;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.client.service.DirectClientHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceHandler;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.ExchangeProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceConnResp;
import io.intellij.netty.tcpfrp.exchange.serversend.ConnLocalResp;
import io.intellij.netty.tcpfrp.exchange.serversend.GetUserData;
import io.intellij.netty.tcpfrp.exchange.serversend.UserBreakConn;
import io.intellij.netty.tcpfrp.exchange.serversend.UserCreateConn;
import io.intellij.netty.utils.CtxUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

import static io.intellij.netty.tcpfrp.exchange.ExchangeType.SERVER_TO_CLIENT_RECEIVE_USER_CONN_BREAK;

/**
 * ExchangeHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class ExchangeHandler extends SimpleChannelInboundHandler<ExchangeProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol msg) throws Exception {
        ExchangeType exchangeType = msg.getExchangeType();

        switch (exchangeType) {

            case SERVER_TO_CLIENT_CONFIG_RESP -> {

                if (ConnLocalResp.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        ConnLocalResp connLocalResp = JSON.parseObject(json, ConnLocalResp.class);
                        log.info("connLocalResp|{}", connLocalResp);
                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    log.error("SERVER_TO_CLIENT_CONFIG_RESP|unknown server msg");
                    ctx.close();
                }

            }

            case SERVER_TO_CLIENT_RECEIVE_USER_CONN_CREATE -> {

                if (UserCreateConn.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        UserCreateConn userCreateConn = JSON.parseObject(json, UserCreateConn.class);
                        String userChannelId = userCreateConn.getUserChannelId();

                        Promise<Channel> serviceChannelPromise = ctx.executor().newPromise();
                        serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                            Channel serviceChannel = future.getNow();
                            if (future.isSuccess()) {
                                log.info("service channel create success");
                                ChannelFuture responseFuture = ctx.channel().writeAndFlush(
                                        ExchangeProtocolUtils.jsonProtocol(
                                                ExchangeType.CLIENT_TO_SERVER_CONN_REAL_SERVICE_SUCCESS,
                                                ServiceConnResp.builder().success(true)
                                                        .serviceChannelId(serviceChannel.id().asLongText())
                                                        .userChannelId(userCreateConn.getUserChannelId())
                                                        .build()
                                        )
                                );
                                responseFuture.addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(@NotNull ChannelFuture future) throws Exception {
                                        ChannelPipeline p = serviceChannel.pipeline();
                                        p.addLast(new ServiceHandler(userCreateConn.getListeningConfig(), userChannelId, ctx.channel()));

                                        p.fireChannelActive();
                                    }
                                });


                            } else {
                                log.error("service channel create failed");
                                ctx.writeAndFlush(
                                        ExchangeProtocolUtils.jsonProtocol(
                                                ExchangeType.CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED,
                                                ServiceConnResp.builder().success(false)
                                                        .serviceChannelId(null)
                                                        .userChannelId(userCreateConn.getUserChannelId())
                                                        .build()
                                        )
                                );
                            }

                        });

                        Channel exchangeChannel = ctx.channel();

                        // connect to service
                        Bootstrap b = new Bootstrap();
                        b.group(exchangeChannel.eventLoop())
                                .channel(NioSocketChannel.class)
                                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                                .option(ChannelOption.SO_KEEPALIVE, true)
                                .handler(new DirectClientHandler(serviceChannelPromise));

                        ListeningConfig listeningConfig = userCreateConn.getListeningConfig();
                        log.info("try connect|{}|{}", listeningConfig.getLocalIp(), listeningConfig.getLocalPort());
                        b.connect(listeningConfig.getLocalIp(), listeningConfig.getLocalPort())
                                .addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(@NotNull ChannelFuture future) throws Exception {
                                        if (future.isSuccess()) {
                                            log.info("connect to service success|{}", listeningConfig);
                                        } else {
                                            log.error("connect to service failed|{}", listeningConfig);
                                            exchangeChannel.writeAndFlush(
                                                    ExchangeProtocolUtils.jsonProtocol(
                                                            ExchangeType.CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED,
                                                            ServiceConnResp.builder().success(false)
                                                                    .serviceChannelId(null)
                                                                    .userChannelId(userCreateConn.getUserChannelId())
                                                                    .build()
                                                    )
                                            );
                                        }
                                    }
                                });
                        log.info("After connect|{}", listeningConfig);
                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    log.error("unknown server msg|SERVER_TO_CLIENT_RECEIVE_USER_CONN_CREATE");
                    ctx.close();
                }

            }

            case SERVER_TO_CLIENT_RECEIVE_USER_CONN_BREAK -> {
                if (UserBreakConn.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        UserBreakConn userBreakConn = JSON.parseObject(json, UserBreakConn.class);
                        String serviceChannelId = userBreakConn.getServiceChannelId();
                        ServiceHandler.closeServiceChannel(serviceChannelId, SERVER_TO_CLIENT_RECEIVE_USER_CONN_BREAK.getDesc());
                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    log.error("unknown server msg|SERVER_TO_CLIENT_RECEIVE_USER_CONN_BREAK");
                    ctx.close();
                }

            }

            case SERVER_TO_CLIENT_GET_USER_DATA -> {

                if (GetUserData.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        GetUserData getUserData = JSON.parseObject(json, GetUserData.class);

                        String serviceChannelId = getUserData.getServiceChannelId();
                        log.info("receive get user data|serviceChannelId={}", serviceChannelId);

                        byte[] data = getUserData.getData();

                        ServiceHandler.dispatch(serviceChannelId, data);

                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    log.error("unknown server msg|SERVER_TO_CLIENT_READ_USER_DATA");
                    ctx.close();
                }

            }

            default -> {
                log.error("unknown type in default case|{}", exchangeType);
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage(), cause);
    }
}
