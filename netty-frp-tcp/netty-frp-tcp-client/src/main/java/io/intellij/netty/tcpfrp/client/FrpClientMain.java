package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.client.handlers.FrpClientInitializer;
import io.intellij.netty.tcpfrp.config.ClientConfig;
import io.intellij.netty.tcpfrp.exchange.SysConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * FrpClientMain
 *
 * @author tech@intellij.io
 */
@Slf4j
public class FrpClientMain {

    public static void main(String[] args) throws InterruptedException {
        ClientConfig clientConfig = ClientConfig.init(ClientConfig.class.getClassLoader().getResourceAsStream("client-config.json"));
        if (clientConfig.isValid()) {
            log.info("client config|{}", clientConfig);
        } else {
            log.error("client config is invalid");
            return;
        }

        SysConfig.logDetails();

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();

            b.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            b.handler(new FrpClientInitializer(clientConfig));
            String serverHost = clientConfig.getServerHost();
            int serverPort = clientConfig.getServerPort();

            ChannelFuture f = b.connect(serverHost, serverPort).sync();

            log.info("Connect to frp-server |host={}|port={}|ssl={}", serverHost, serverPort, clientConfig.isSsl());

            f.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }

    }

}
