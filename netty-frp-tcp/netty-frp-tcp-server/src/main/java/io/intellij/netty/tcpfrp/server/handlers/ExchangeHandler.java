package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.c2s.ListeningConfigReport;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceBreakConn;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnFailed;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnSuccess;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.codec.ProtocolParse;
import io.intellij.netty.tcpfrp.exchange.s2c.ListeningLocalResp;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.MultiPortUtils;
import io.intellij.netty.tcpfrp.server.listening.UserHandler;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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
        // frp server 获取到 client 读取的 service 的数据
        switch (exchangeType) {

            case C2S_SEND_CONFIG -> {

                ProtocolParse<ListeningConfigReport> parse = ExchangeProtocolUtils.parseProtocolBy(msg, ListeningConfigReport.class);

                if (parse.isValid()) {
                    ListeningConfigReport sendListeningConfig = parse.getData();
                    log.info("get frp-client's listening config request|{}", sendListeningConfig);
                    this.prepareMultiPortNettyServer(ctx, sendListeningConfig.getListeningConfigMap());
                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }
            }

            // frp client 连接服务成功 回复的消息
            case C2S_CONN_REAL_SERVICE_SUCCESS -> {

                ProtocolParse<ServiceConnSuccess> parse = ExchangeProtocolUtils.parseProtocolBy(msg, ServiceConnSuccess.class);

                if (parse.isValid()) {
                    ServiceConnSuccess serviceConnSuccess = parse.getData();
                    log.info("ConnServiceResp|frp-client connect service success|{}", serviceConnSuccess);
                    // 远程连接成功了
                    String userChannelId = serviceConnSuccess.getUserChannelId();
                    String serviceChannelId = serviceConnSuccess.getServiceChannelId();
                    UserHandler.notifyUserChannelRead(userChannelId, serviceChannelId);

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            // frp client 连接服务失败
            case C2S_CONN_REAL_SERVICE_FAILED -> {

                ProtocolParse<ServiceConnFailed> parse = ExchangeProtocolUtils.parseProtocolBy(msg, ServiceConnFailed.class);

                if (parse.isValid()) {
                    ServiceConnFailed serviceConnFailed = parse.getData();
                    log.error("ConnServiceResp|frp-client connect service failed|{}", serviceConnFailed);

                    String userChannelId = serviceConnFailed.getUserChannelId();
                    UserHandler.closeUserChannel(userChannelId, parse.getExchangeType().getDesc());

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            case C2S_LOST_REAL_SERVER_CONN -> {

                ProtocolParse<ServiceBreakConn> parse = ExchangeProtocolUtils.parseProtocolBy(msg, ServiceBreakConn.class);

                if (parse.isValid()) {
                    ServiceBreakConn serviceBreakConn = parse.getData();
                    log.error("ConnServiceResp|frp-client lost service's connection|{}", serviceBreakConn);
                    String userChannelId = serviceBreakConn.getUserChannelId();

                    UserHandler.closeUserChannel(userChannelId, parse.getExchangeType().getDesc());

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            // 处理数据 传输使用的JSON序列化
            case C2S_SERVICE_DATA_PACKET -> {
                if (!dataPacketUseJson) {
                    throw new RuntimeException("data packet parse type is not json !!!");
                }
                ProtocolParse<DataPacket> parse = ExchangeProtocolUtils.parseProtocolBy(msg, DataPacket.class);
                if (parse.isValid()) {
                    DataPacket serviceDataPacket = parse.getData();

                    String userChannelId = serviceDataPacket.getUserChannelId();
                    byte[] data = serviceDataPacket.getPacket();

                    UserHandler.dispatch(userChannelId, Unpooled.copiedBuffer(data));
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

    private final AtomicReference<MultiPortNettyServer> serverRef = new AtomicReference<>(null);

    private void prepareMultiPortNettyServer(ChannelHandlerContext ctx, Map<String, ListeningConfig> listeningConfigMap) {
        ListeningLocalResp listeningLocalResp = MultiPortUtils.connLocalResp(listeningConfigMap.values().stream().toList());
        ctx.writeAndFlush(ExchangeProtocolUtils.buildProtocolByJson(ExchangeType.S2C_LISTENING_CONFIG_RESP, listeningLocalResp));
        if (!listeningLocalResp.isSuccess()) {
            ctx.close();
        } else {
            MultiPortNettyServer server = new MultiPortNettyServer(listeningConfigMap, ctx.channel());
            if (server.start()) {
                log.info("multi port server start ...");
                serverRef.set(server);
            } else {
                ctx.close();
            }
        }
    }

    private void stopMultiPortNettyServer() {
        MultiPortNettyServer mServer = serverRef.get();
        if (mServer != null) {
            mServer.stop();
            serverRef.set(null);
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        this.stopMultiPortNettyServer();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause);
    }

}
