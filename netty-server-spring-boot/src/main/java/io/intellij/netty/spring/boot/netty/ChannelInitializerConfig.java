package io.intellij.netty.spring.boot.netty;

import io.intellij.netty.spring.boot.netty.handlers.EchoHandler;
import io.intellij.netty.spring.boot.netty.handlers.LogHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * ChannelInitializerConfig
 *
 * @author tech@intellij.io
 * @since 2025-05-08
 */
@Component
public class ChannelInitializerConfig {

    @Bean(name = "log")
    public ChannelHandler logChannelHandler() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LogHandler());
            }
        };
    }

    @Bean(name = "echo")
    public ChannelHandler echoChannelHandler() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new EchoHandler());
            }
        };
    }

}
