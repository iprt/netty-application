package io.intellij.netty.tcpfrp.server.handlers;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.ExchangeProtocol;
import io.intellij.netty.tcpfrp.exchange.ExchangeProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.clientsend.GetServiceData;
import io.intellij.netty.tcpfrp.exchange.clientsend.SendListeningConfig;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceBreakConn;
import io.intellij.netty.tcpfrp.exchange.clientsend.ServiceConnResp;
import io.intellij.netty.tcpfrp.exchange.serversend.ConnLocalResp;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.MultiPortUtils;
import io.intellij.netty.tcpfrp.server.user.UserHandler;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.intellij.netty.tcpfrp.exchange.ExchangeType.CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED;
import static io.intellij.netty.tcpfrp.exchange.ExchangeType.CLIENT_TO_SERVER_LOST_REAL_SERVER_CONN;

/**
 * UserHandlers
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ExchangeHandler extends SimpleChannelInboundHandler<ExchangeProtocol> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ExchangeProtocol msg) throws Exception {

        ExchangeType exchangeType = msg.getExchangeType();

        switch (exchangeType) {

            case CLIENT_TO_SERVER_SEND_CONFIG -> {
                if (SendListeningConfig.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        SendListeningConfig sendListeningConfig = JSON.parseObject(json, SendListeningConfig.class);
                        log.info("listening config|{}", sendListeningConfig);
                        this.doReceiveClientConfig(ctx, sendListeningConfig.getListeningConfigMap());
                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    ctx.close();
                }

            }

            // frp client 连接服务成功
            case CLIENT_TO_SERVER_CONN_REAL_SERVICE_SUCCESS -> {
                if (ServiceConnResp.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        ServiceConnResp serviceConnResp = JSON.parseObject(json, ServiceConnResp.class);
                        log.info("ConnServiceResp Get Success|{}", serviceConnResp);
                        log.info("得到frp-client回复的消息，连接成功");

                        // 远程连接成功了
                        String userChannelId = serviceConnResp.getUserChannelId();
                        String serviceChannelId = serviceConnResp.getServiceChannelId();

                        UserHandler.notifyUserChannelRead(userChannelId, serviceChannelId);

                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }
                } else {
                    ctx.close();
                }

            }

            // frp client 连接服务失败
            case CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED -> {
                if (ServiceConnResp.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        ServiceConnResp serviceConnResp = JSON.parseObject(json, ServiceConnResp.class);
                        log.error("ConnServiceResp Get Failed|{}", serviceConnResp);

                        String userChannelId = serviceConnResp.getUserChannelId();
                        UserHandler.closeUserChannel(userChannelId, CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED.getDesc());

                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }
                } else {
                    ctx.close();
                }
            }

            case CLIENT_TO_SERVER_LOST_REAL_SERVER_CONN -> {

                if (ServiceBreakConn.class.getName().equals(msg.getClassName())) {
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        ServiceBreakConn serviceBreakConn = JSON.parseObject(json, ServiceBreakConn.class);

                        String userChannelId = serviceBreakConn.getUserChannelId();
                        UserHandler.closeUserChannel(userChannelId, CLIENT_TO_SERVER_LOST_REAL_SERVER_CONN.getDesc());

                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    ctx.close();
                }

            }

            // frp server 获取到 client 读取的 service 的数据
            case CLIENT_TO_SERVER_GET_SERVICE_DATA -> {
                if (GetServiceData.class.getName().equals(msg.getClassName())) {
                    log.info("receive get service data");
                    String json = new String(msg.getBody(), StandardCharsets.UTF_8);
                    try {
                        GetServiceData serviceData = JSON.parseObject(json, GetServiceData.class);

                        String userChannelId = serviceData.getUserChannelId();
                        byte[] data = serviceData.getData();

                        UserHandler.dispatch(userChannelId, Unpooled.copiedBuffer(data));

                    } catch (Exception e) {
                        log.error("", e);
                        ctx.close();
                    }

                } else {
                    ctx.close();
                }

            }

            default -> {
                log.error("unknown type in default case|{}", exchangeType);
                ctx.close();
            }
        }

    }

    private final AtomicReference<MultiPortNettyServer> serverRef = new AtomicReference<>(null);

    private void doReceiveClientConfig(ChannelHandlerContext ctx, Map<String, ListeningConfig> listeningConfigMap) {
        ConnLocalResp connLocalResp = MultiPortUtils.connLocalResp(listeningConfigMap.values().stream().toList());
        ctx.writeAndFlush(ExchangeProtocolUtils.jsonProtocol(ExchangeType.SERVER_TO_CLIENT_CONFIG_RESP, connLocalResp));
        if (!connLocalResp.isSuccess()) {
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
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }
}
