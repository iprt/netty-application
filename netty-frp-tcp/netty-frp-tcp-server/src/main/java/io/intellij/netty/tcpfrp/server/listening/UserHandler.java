package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocolDataPacket;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeProtocolUtils;
import io.intellij.netty.tcpfrp.exchange.codec.ExchangeType;
import io.intellij.netty.tcpfrp.exchange.s2c.UserBreakConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserCreateConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserDataPacket;
import io.intellij.netty.utils.ByteUtils;
import io.intellij.netty.utils.ChannelUtils;
import io.intellij.netty.utils.ConnHostPort;
import io.intellij.netty.utils.CtxUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.intellij.netty.tcpfrp.exchange.SysConfig.DATA_PACKET_USE_JSON;
import static io.intellij.netty.tcpfrp.exchange.SysConfig.ENABLE_DISPATCH_LOG;
import static io.intellij.netty.tcpfrp.exchange.SysConfig.ENABLE_RANDOM_TYPE;
import static io.intellij.netty.tcpfrp.exchange.codec.ExchangeType.encodeRealToRandom;

/**
 * UserHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class UserHandler extends ChannelInboundHandlerAdapter {
    private static final Map<String, Channel> userChannelMap = new ConcurrentHashMap<>();
    private static final Map<String, String> userChannelId2ServiceChannelId = new ConcurrentHashMap<>();

    private final Map<Integer, ListeningConfig> portToServer;
    private final Channel exchangeChannel;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // e.g. user -sync-> localhost:3306
        String userChannelId = CtxUtils.getChannelId(ctx);
        userChannelMap.put(userChannelId, ctx.channel());

        ConnHostPort localInfo = CtxUtils.getLocalAddress(ctx);
        ListeningConfig listeningConfig = portToServer.get(localInfo.getPort());
        // user conn listening server
        if (exchangeChannel.isActive()) {
            exchangeChannel.writeAndFlush(
                    ExchangeProtocolUtils.buildProtocolByJson(
                            ExchangeType.S2C_RECEIVE_USER_CONN_CREATE,
                            UserCreateConn.builder()
                                    .listeningConfig(listeningConfig).userChannelId(userChannelId)
                                    .build()
                    )
            );
        }
    }

    // AUTO_READ = false
    public static void notifyUserChannelRead(String userChannelId, String serviceChannelId) {
        userChannelId2ServiceChannelId.put(userChannelId, serviceChannelId);
        Channel channel = userChannelMap.get(userChannelId);
        if (channel != null) {
            if (channel.isActive()) {
                channel.read();
            }
        }
    }

    // childOption(ChannelOption.AUTO_READ, false)
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object objMsg) throws Exception {
        // e.g. user -write-> localhost:3306

        if (objMsg instanceof ByteBuf msg) {
            Channel userChannel = ctx.channel();
            String userChannelId = CtxUtils.getChannelId(ctx);

            // take service channel id
            String serviceChannelId = userChannelId2ServiceChannelId.get(userChannelId);

            if (serviceChannelId == null) {
                log.error("Internal error occurred <serviceChannelId=null> |userChannelId={}", userChannelId);
                ctx.close();
                return;
            }

            if (DATA_PACKET_USE_JSON) {
                byte[] bytes = new byte[msg.readableBytes()];
                msg.readBytes(bytes);
                exchangeChannel.writeAndFlush(
                        ExchangeProtocolUtils.buildProtocolByJson(
                                ExchangeType.S2C_USER_DATA_PACKET,
                                DataPacket.builder()
                                        .from(UserDataPacket.class.getName())
                                        .userChannelId(userChannelId).serviceChannelId(serviceChannelId)
                                        .packet(bytes).build()
                        )
                ).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        if (userChannel.isActive()) {
                            userChannel.read();
                        }
                    } else {
                        closeUserChannel(userChannelId, "UserHandler.channelRead0");
                    }
                });

            } else {
                byte[] userChannelIdBytes = userChannelId.getBytes();
                byte[] serviceChannelIdBytes = serviceChannelId.getBytes();

                byte[] prepareBytes = new byte[1 + 4 + userChannelIdBytes.length + serviceChannelIdBytes.length];

                int bodyLen = userChannelIdBytes.length + serviceChannelIdBytes.length + msg.readableBytes();
                byte[] bodyLenBytes = ByteUtils.getIntBytes(bodyLen);

                int dataPacketType = ExchangeType.S2C_USER_DATA_PACKET.getType();
                prepareBytes[0] = (byte) (ENABLE_RANDOM_TYPE ? encodeRealToRandom(dataPacketType) : dataPacketType);

                System.arraycopy(bodyLenBytes, 0, prepareBytes, 1, bodyLenBytes.length);
                System.arraycopy(userChannelIdBytes, 0, prepareBytes, 1 + 4, userChannelIdBytes.length);
                System.arraycopy(serviceChannelIdBytes, 0, prepareBytes, 1 + 4 + userChannelIdBytes.length, serviceChannelIdBytes.length);

                // write 1
                exchangeChannel.write(Unpooled.copiedBuffer(prepareBytes));

                // 引用计数器+1 给exchangeChannel使用,节约内存
                // ReferenceCountUtil.retain(msg);

                // write 2
                // exchangeChannel.write(msg.retainedDuplicate())
                exchangeChannel.write(objMsg)
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                if (userChannel.isActive()) {
                                    userChannel.read();
                                }
                            } else {
                                closeUserChannel(userChannelId, "ServiceHandler.channelRead0");
                            }
                        });
                exchangeChannel.flush();
            }
        } else {
            ctx.close();
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        String userChannelId = CtxUtils.getChannelId(ctx);
        ConnHostPort localInfo = CtxUtils.getLocalAddress(ctx);

        ListeningConfig listeningConfig = portToServer.get(localInfo.getPort());

        if (exchangeChannel.isActive()) {
            exchangeChannel.writeAndFlush(
                    ExchangeProtocolUtils.buildProtocolByJson(ExchangeType.S2C_RECEIVE_USER_CONN_BREAK,
                            UserBreakConn.builder()
                                    .listeningConfig(listeningConfig)
                                    .userChannelId(userChannelId)
                                    .build()
                    )
            );
            closeUserChannel(userChannelId, "UserHandler.channelInactive");
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}|{}", CtxUtils.getRemoteAddress(ctx), cause.getMessage());
    }

    public static void closeUserChannel(String userChannelId, String desc) {
        if (userChannelId == null) {
            return;
        }
        Channel channel = userChannelMap.get(userChannelId);
        if (channel != null) {
            log.info("CloseUserChannel|UserChannelId={}|desc={}", userChannelId, desc);
            ChannelUtils.closeOnFlush(channel);
            userChannelMap.remove(userChannelId);
        }

        userChannelId2ServiceChannelId.remove(userChannelId);
    }

    public static void dispatch(String userChannelId, ByteBuf msg) {
        Channel userChannel = userChannelMap.get(userChannelId);
        if (userChannel != null && userChannel.isActive()) {
            log.info("dispatch to user|userChannelId={}", userChannelId);
            userChannel.writeAndFlush(msg)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            future.channel().read();
                        } else {
                            future.channel().close();
                        }
                    });
        }
    }

    public static void dispatch(ExchangeProtocolDataPacket dataPacket) {
        String userChannelId = dataPacket.userChannelId();
        Channel userChannel = userChannelMap.get(userChannelId);
        if (userChannel != null && userChannel.isActive()) {
            if (ENABLE_DISPATCH_LOG) {
                log.info("dispatch ByteBuf to user|userChannelId={}|realPacketLen={}", userChannelId, dataPacket.packet().readableBytes());
            }
            userChannel.writeAndFlush(dataPacket.packet())
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            future.channel().read();
                        } else {
                            future.channel().close();
                        }
                    });
        }
    }

}
