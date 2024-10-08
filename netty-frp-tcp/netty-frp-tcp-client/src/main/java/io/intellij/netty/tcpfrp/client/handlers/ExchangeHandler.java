package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.client.service.DirectServiceHandler;
import io.intellij.netty.tcpfrp.client.service.ServiceHandler;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnFailed;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnSuccess;
import io.intellij.netty.tcpfrp.exchange.codec.ExProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.codec.ProtocolParse;
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

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol msg) throws Exception {
        ExchangeType exchangeType = msg.getExchangeType();
        final Channel exchangeChannel = ctx.channel();

        switch (exchangeType) {

            case S2C_LISTENING_CONFIG_RESP -> {
                ProtocolParse<ListeningLocalResp> parse = ExProtocolUtils.parseProtocol(msg, ListeningLocalResp.class);
                if (parse.isValid()) {
                    log.info("frp-server response|{}", parse.getData());
                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }
            }

            case S2C_RECEIVE_USER_CONN_CREATE -> {
                ProtocolParse<UserCreateConn> parse = ExProtocolUtils.parseProtocol(msg, UserCreateConn.class);
                if (parse.isValid()) {
                    UserCreateConn userCreateConn = parse.getData();
                    final String userChannelId = userCreateConn.getUserChannelId();

                    Promise<Channel> serviceChannelPromise = ctx.executor().newPromise();
                    serviceChannelPromise.addListener((FutureListener<Channel>) future -> {
                        Channel serviceChannel = future.getNow();
                        if (future.isSuccess()) {
                            String serviceChannelId = serviceChannel.id().asLongText();
                            log.info("service channel create success");
                            ChannelFuture responseFuture = exchangeChannel.writeAndFlush(
                                    ExProtocolUtils.createProtocolData(
                                            ExchangeType.C2S_CONN_REAL_SERVICE_SUCCESS,
                                            ServiceConnSuccess.builder().success(true)
                                                    .serviceChannelId(serviceChannelId).userChannelId(userChannelId)
                                                    .build()
                                    )
                            );
                            responseFuture.addListener((ChannelFutureListener) f -> {
                                if (f.isSuccess()) {
                                    ChannelPipeline p = serviceChannel.pipeline();
                                    p.addLast(
                                            new ServiceHandler(userCreateConn.getListeningConfig(), userChannelId, exchangeChannel)
                                    );
                                    p.fireChannelActive();
                                }
                            });

                        } else {
                            log.error("service channel create failed");
                            ctx.writeAndFlush(ExProtocolUtils.createProtocolData(
                                    ExchangeType.C2S_CONN_REAL_SERVICE_FAILED,
                                    ServiceConnFailed.builder().success(false)
                                            .serviceChannelId(null).userChannelId(userChannelId)
                                            .build())
                            );
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
                                            ExProtocolUtils.createProtocolData(
                                                    ExchangeType.C2S_CONN_REAL_SERVICE_FAILED,
                                                    ServiceConnFailed.builder().success(false)
                                                            .serviceChannelId(null)
                                                            .userChannelId(userCreateConn.getUserChannelId())
                                                            .build()
                                            )
                                    );
                                }
                            });
                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            case S2C_RECEIVE_USER_CONN_BREAK -> {
                ProtocolParse<UserBreakConn> parse = ExProtocolUtils.parseProtocol(msg, UserBreakConn.class);
                if (parse.isValid()) {
                    UserBreakConn userBreakConn = parse.getData();
                    String serviceChannelId = userBreakConn.getServiceChannelId();
                    ServiceHandler.closeServiceChannel(serviceChannelId, parse.getExchangeType().getDesc());

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            case S2C_USER_DATA_PACKET -> {
                // ProtocolParse<UserDataPacket> parse = ExProtocolUtils.parseProtocol(msg, UserDataPacket.class);
                ProtocolParse<DataPacket> parse = ExProtocolUtils.parseDataPacket(msg);

                if (parse.isValid()) {
                    DataPacket userDataPacket = parse.getData();

                    String serviceChannelId = userDataPacket.getServiceChannelId();

                    // log.info("receive get user data|serviceChannelId={}", serviceChannelId);
                    ServiceHandler.dispatch(serviceChannelId, userDataPacket.getPacket());

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
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
        log.error("localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause);
    }
}
