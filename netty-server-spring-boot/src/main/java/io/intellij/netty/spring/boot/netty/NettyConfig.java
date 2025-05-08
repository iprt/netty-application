package io.intellij.netty.spring.boot.netty;

import io.intellij.netty.spring.boot.netty.handlers.EchoHandler;
import io.intellij.netty.spring.boot.netty.handlers.LogHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * NettyInitialConfig
 *
 * @author tech@intellij.io
 */
@Configuration
public class NettyConfig {

    @Bean
    public EventLoopGroup bossGroup() {
        return new NioEventLoopGroup(1);
    }

    @Bean
    public EventLoopGroup workerGroup() {
        return new NioEventLoopGroup();
    }

}
