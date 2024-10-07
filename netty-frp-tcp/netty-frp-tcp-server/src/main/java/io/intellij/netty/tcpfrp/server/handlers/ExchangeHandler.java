package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.ExProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.ProtocolParse;
import io.intellij.netty.tcpfrp.exchange.clientsend.ListeningConfigReport;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceBreakConn;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceConnResp;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceDataPacket;
import io.intellij.netty.tcpfrp.exchange.serversend.ListeningLocalResp;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.MultiPortUtils;
import io.intellij.netty.tcpfrp.server.user.UserHandler;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ExchangeHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ExchangeHandler extends SimpleChannelInboundHandler<ExchangeProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol msg) throws Exception {
        ExchangeType exchangeType = msg.getExchangeType();
        switch (exchangeType) {

            case C2S_SEND_CONFIG -> {
                ProtocolParse<ListeningConfigReport> parse = ExProtocolUtils.parseObj(msg, ListeningConfigReport.class);
                if (parse.isValid()) {
                    ListeningConfigReport sendListeningConfig = parse.getData();
                    log.info("get frp-client's listening config request|{}", sendListeningConfig);
                    this.prepareMultiPortNettyServer(ctx, sendListeningConfig.getListeningConfigMap());
                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }
            }

            // frp client 连接服务成功
            case C2S_CONN_REAL_SERVICE_SUCCESS -> {

                ProtocolParse<ServiceConnResp> parse = ExProtocolUtils.parseObj(msg, ServiceConnResp.class);

                if (parse.isValid()) {
                    ServiceConnResp serviceConnResp = parse.getData();
                    log.info("ConnServiceResp|frp-client connect service success|{}", serviceConnResp);
                    // 远程连接成功了
                    String userChannelId = serviceConnResp.getUserChannelId();
                    String serviceChannelId = serviceConnResp.getServiceChannelId();
                    UserHandler.notifyUserChannelRead(userChannelId, serviceChannelId);

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            // frp client 连接服务失败
            case C2S_CONN_REAL_SERVICE_FAILED -> {

                ProtocolParse<ServiceConnResp> parse = ExProtocolUtils.parseObj(msg, ServiceConnResp.class);

                if (parse.isValid()) {
                    ServiceConnResp serviceConnResp = parse.getData();
                    log.error("ConnServiceResp|frp-client connect service failed|{}", serviceConnResp);

                    String userChannelId = serviceConnResp.getUserChannelId();
                    UserHandler.closeUserChannel(userChannelId, parse.getExchangeType().getDesc());

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            case C2S_LOST_REAL_SERVER_CONN -> {

                ProtocolParse<ServiceBreakConn> parse = ExProtocolUtils.parseObj(msg, ServiceBreakConn.class);

                if (parse.isValid()) {
                    ServiceBreakConn serviceBreakConn = parse.getData();
                    log.error("ConnServiceResp|frp-client lost service's connection|{}", serviceBreakConn);
                    String userChannelId = serviceBreakConn.getUserChannelId();

                    UserHandler.closeUserChannel(userChannelId, parse.getExchangeType().getDesc());

                } else {
                    throw new RuntimeException(parse.getInvalidMsg());
                }

            }

            // frp server 获取到 client 读取的 service 的数据
            case C2S_SERVICE_DATA_PACKET -> {

                ProtocolParse<ServiceDataPacket> parse = ExProtocolUtils.parseObj(msg, ServiceDataPacket.class);

                if (parse.isValid()) {
                    ServiceDataPacket serviceData = parse.getData();

                    String userChannelId = serviceData.getUserChannelId();
                    byte[] data = serviceData.getPacket();

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
        ctx.writeAndFlush(ExProtocolUtils.jsonProtocol(ExchangeType.S2C_LISTENING_CONFIG_RESP, listeningLocalResp));
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

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        MultiPortNettyServer mServer = serverRef.get();
        if (mServer != null) {
            mServer.stop();
            serverRef.set(null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("localAddress={}|remoteAddress={}", CtxUtils.getLocalAddress(ctx), CtxUtils.getRemoteAddress(ctx), cause);
    }
}
