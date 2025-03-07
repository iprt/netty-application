package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.server.handlers.dispatch.DispatchToUserHandler;
import io.intellij.netty.tcpfrp.server.handlers.dispatch.ReceiveServiceStateHandler;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.MultiPortUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public static final AttributeKey<MultiPortNettyServer> MULTI_PORT_NETTY_SERVER_KEY = AttributeKey.valueOf("multiPortNettyServer");

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        // Triggered from ServerAuthHandler
        ctx.read();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ListeningRequest listeningRequest) throws Exception {
        log.info("get listening request: {}", listeningRequest);
        List<Integer> listeningPorts = listeningRequest.getListeningPorts();
        // 测试可以监听
        ListeningResponse test = MultiPortUtils.test(listeningPorts);
        if (test.isSuccess()) {
            MultiPortNettyServer multiPortNettyServer = new MultiPortNettyServer(listeningPorts, ctx.channel());
            if (multiPortNettyServer.start()) {
                ctx.writeAndFlush(FrpBasicMsg.createListeningResponse(test)).addListener(
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                // remote this
                                ctx.pipeline().remove(ListeningRequestHandler.class);

                                ctx.channel().attr(MULTI_PORT_NETTY_SERVER_KEY).set(multiPortNettyServer);
                                ctx.pipeline()
                                        .addLast(new ReceiveServiceStateHandler())
                                        .addLast(new DispatchToUserHandler());

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
