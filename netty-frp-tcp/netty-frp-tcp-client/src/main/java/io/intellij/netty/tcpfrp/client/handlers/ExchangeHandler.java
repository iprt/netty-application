package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.client.service.DirectServiceHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceHandler;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnFailed;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnSuccess;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.s2c.ListeningLocalResp;
import io.intellij.netty.tcpfrp.exchange.s2c.UserBreakConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserCreateConn;
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

/**
 * ExchangeHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class ExchangeHandler extends SimpleChannelInboundHandler<ExchangeProtocol> {
    private final boolean dataPacketUseJson;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol msg) throws Exception {
        ExchangeType exchangeType = msg.exchangeType();
        final Channel exchangeChannel = ctx.channel();

        // 事件处理
        switch (exchangeType) {

            case S2C_LISTENING_CONFIG_RESP -> {
                ExchangeProtocolUtils.ProtocolParse<ListeningLocalResp> parse = ExchangeProtocolUtils.parseProtocolByJson(msg, ListeningLocalResp.class);
                if (parse.valid()) {
                    log.info("frp-server response|{}", parse.data());
                } else {
                    throw new RuntimeException(parse.invalidMsg());
                }
            }

            case S2C_RECEIVE_USER_CONN_CREATE -> {
                ExchangeProtocolUtils.ProtocolParse<UserCreateConn> parse = ExchangeProtocolUtils.parseProtocolByJson(msg, UserCreateConn.class);
                if (parse.valid()) {
                    UserCreateConn userCreateConn = parse.data();
                    final String userChannelId = userCreateConn.getUserChannelId();

                    Promise<Channel> serviceChannelPromise = ctx.executor().newPromise();
                    serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                        Channel serviceChannel = future.getNow();
                        if (future.isSuccess()) {
                            String serviceChannelId = serviceChannel.id().asLongText();
                            log.info("service channel create success");
                            ChannelFuture responseFuture = exchangeChannel.writeAndFlush(
                                    ExchangeProtocolUtils.buildProtocolByJson(
                                            ExchangeType.C2S_CONN_REAL_SERVICE_SUCCESS,
                                            ServiceConnSuccess.create(userChannelId, serviceChannelId)
                                    )
                            );
                            responseFuture.addListener((ChannelFutureListener) f -> {
                                if (f.isSuccess()) {
                                    ChannelPipeline p = serviceChannel.pipeline();
                                    p.addLast(
                                            new ServiceHandler(
                                                    userCreateConn.getListeningConfig(), userChannelId, exchangeChannel,
                                                    dataPacketUseJson
                                            )
                                    );
                                    p.fireChannelActive();
                                }
                            });

                        } else {
                            log.error("service channel create failed");
                            ctx.writeAndFlush(ExchangeProtocolUtils.buildProtocolByJson(
                                    ExchangeType.C2S_CONN_REAL_SERVICE_FAILED,
                                    ServiceConnFailed.create(userChannelId)
                            ));
                        }

                    });

                    // connect to service
                    Bootstrap b = new Bootstrap();
                    b.group(exchangeChannel.eventLoop())
                            .channel(NioSocketChannel.class)
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                            .option(ChannelOption.AUTO_READ, false)
                            .handler(new DirectServiceHandler(serviceChannelPromise));

                    ListeningConfig listeningConfig = userCreateConn.getListeningConfig();
                    log.info("bootstrap try to connect {}:{}", listeningConfig.getLocalIp(), listeningConfig.getLocalPort());
                    b.connect(listeningConfig.getLocalIp(), listeningConfig.getLocalPort())
                            .addListener((ChannelFutureListener) future -> {
                                if (future.isSuccess()) {
                                    log.info("connect to service success|{}", listeningConfig);
                                } else {
                                    log.error("connect to service failed|{}", listeningConfig);
                                    exchangeChannel.writeAndFlush(
                                            ExchangeProtocolUtils.buildProtocolByJson(
                                                    ExchangeType.C2S_CONN_REAL_SERVICE_FAILED,
                                                    ServiceConnFailed.create(userChannelId)
                                            )
                                    );
                                }
                            });
                } else {
                    throw new RuntimeException(parse.invalidMsg());
                }

            }

            case S2C_RECEIVE_USER_CONN_BREAK -> {
                ExchangeProtocolUtils.ProtocolParse<UserBreakConn> parse = ExchangeProtocolUtils.parseProtocolByJson(msg, UserBreakConn.class);
                if (parse.valid()) {
                    UserBreakConn userBreakConn = parse.data();
                    String serviceChannelId = userBreakConn.getServiceChannelId();
                    ServiceHandler.closeServiceChannel(serviceChannelId, parse.exchangeType().getDesc());
                } else {
                    throw new RuntimeException(parse.invalidMsg());
                }
            }

            case S2C_USER_DATA_PACKET -> {
                if (!dataPacketUseJson) {
                    throw new RuntimeException("data packet parse type is not json !!!");
                }

                ExchangeProtocolUtils.ProtocolParse<DataPacket> parse = ExchangeProtocolUtils.parseProtocolByJson(msg, DataPacket.class);
                if (parse.valid()) {
                    DataPacket userDataPacket = parse.data();
                    String serviceChannelId = userDataPacket.getServiceChannelId();
                    // log.info("receive get user data|serviceChannelId={}", serviceChannelId);
                    ServiceHandler.dispatch(serviceChannelId, userDataPacket.getPacket());

                } else {
                    throw new RuntimeException(parse.invalidMsg());
                }
            }

            default -> {
                log.error("unknown type in default case|{}", exchangeType);
                ctx.close();
            }

        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ServiceHandler.closeAllServiceChannels();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught|localAddress={}|remoteAddress={}|{}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }
}
