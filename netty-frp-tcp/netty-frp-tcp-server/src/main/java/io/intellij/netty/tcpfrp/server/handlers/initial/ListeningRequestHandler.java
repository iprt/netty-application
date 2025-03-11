package io.intellij.netty.tcpfrp.server.handlers.initial;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.server.handlers.dispatch.DispatchToUserHandler;
import io.intellij.netty.tcpfrp.server.handlers.dispatch.ReceiveServiceStateHandler;
import io.intellij.netty.tcpfrp.server.listening.MultiPortNettyServer;
import io.intellij.netty.tcpfrp.server.listening.MultiPortUtils;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
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

    /**
     * Triggered from {@link AuthRequestHandler}
     */
    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }

    @Override
    protected void channelRead0(@NotNull ChannelHandlerContext ctx, ListeningRequest listeningRequest) throws Exception {
        FrpChannel frpChannel = FrpChannel.get(ctx.channel());

        log.info("get listening request: {}", listeningRequest);
        List<Integer> listeningPorts = listeningRequest.getListeningPorts();
        // 测试可以监听
        ListeningResponse test = MultiPortUtils.test(listeningPorts);
        if (test.isSuccess()) {
            MultiPortNettyServer server = new MultiPortNettyServer(listeningPorts, frpChannel);
            if (server.start()) {
                frpChannel.write(FrpBasicMsg.createListeningResponse(test)).addListener(
                        (ChannelFutureListener) f -> {
                            if (f.isSuccess()) {
                                // remote this
                                ChannelPipeline p = ctx.pipeline();
                                p.remove(this);

                                MultiPortNettyServer.set(ctx.channel(), server);

                                p.addLast(new PingHandler())
                                        .addLast(new ReceiveServiceStateHandler())
                                        .addLast(new DispatchToUserHandler());

                                p.fireChannelActive();
                            } else {
                                frpChannel.close();
                            }
                        }
                );
            } else {
                test.setSuccess(false);
                test.setReason("start multi port netty server failed");
                frpChannel.write(FrpBasicMsg.createListeningResponse(test))
                        .addListener(ChannelFutureListener.CLOSE);
            }

        } else {
            frpChannel.write(FrpBasicMsg.createListeningResponse(test))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("stop multi port server");
        MultiPortNettyServer.stop(ctx.channel());
    }
}
