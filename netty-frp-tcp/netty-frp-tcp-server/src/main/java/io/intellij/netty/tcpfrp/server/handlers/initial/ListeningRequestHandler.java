package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.server.handlers.dispatch.ServerDispatchHandler;
import io.intellij.netty.tcpfrp.server.handlers.dispatch.ServiceConnStateHandler;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.MultiPortUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static io.intellij.netty.tcpfrp.server.handlers.FrpServerInitializer.MULTI_PORT_NETTY_SERVER_KEY;

/**
 * ListeningRequestHandler
 * <p>
 * This handler is responsible for processing listening requests from the client.
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@Slf4j
public class ListeningRequestHandler extends SimpleChannelInboundHandler<ListeningRequest> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // from ServerAuthHandler
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ListeningRequest msg) throws Exception {
        log.info("get listening request: {}", msg);
        Map<String, ListeningConfig> configMap = msg.getConfigMap();
        List<ListeningConfig> configList = configMap.values().stream().toList();

        // 测试可以监听
        ListeningResponse test = MultiPortUtils.test(configList);

        if (test.isSuccess()) {
            MultiPortNettyServer multiPortNettyServer = new MultiPortNettyServer(msg.getConfigMap(), ctx.channel());
            if (multiPortNettyServer.start()) {
                ctx.writeAndFlush(FrpBasicMsg.createListeningResponse(test)).addListener(
                        (ChannelFutureListener) cf -> {
                            if (cf.isSuccess()) {
                                // remote this
                                ctx.pipeline().remove(ListeningRequestHandler.class);

                                ctx.channel().attr(MULTI_PORT_NETTY_SERVER_KEY).set(multiPortNettyServer);
                                ctx.pipeline().addLast(new ServiceConnStateHandler())
                                        .addLast(new ServerDispatchHandler());

                                // {@link ServiceConnStateHandler}
                                ctx.pipeline().fireChannelActive();
                            } else {
                                ctx.close();
                            }
                        }
                );

            } else {
                test.setSuccess(false);
                test.setReason("start multi port netty server failed");
                ctx.writeAndFlush(test).addListener(ChannelFutureListener.CLOSE);
            }

        } else {
            ctx.writeAndFlush(test).addListener(ChannelFutureListener.CLOSE);
        }
    }

}
