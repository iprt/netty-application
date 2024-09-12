package io.intellij.netty.server.tcpproxy.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;


/**
 * HexDumpProxyInitializer
 *
 * @author tech@intellij.io
 */
public class HexDumpProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;

    public HexDumpProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ch.pipeline().addLast(
                new LoggingHandler(LogLevel.INFO)
        ).addLast(
                new HexDumpProxyFrontendHandler(remoteHost, remotePort)
        );
    }

}
