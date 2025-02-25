/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.intellij.netty.server.socks;

import io.intellij.netty.server.socks.handler.SocksServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class SocksServer {
    private static final int PORT = Integer.parseInt(System.getProperty("port", "1080"));

    private static final boolean ENABLE_AUTH = Boolean.parseBoolean(System.getProperty("enableAuth", "false"));

    private static final boolean ENABLE_EPOLL = Boolean.parseBoolean(System.getProperty("enableEpoll", "false"));

    public static void main(String[] args) throws Exception {
        if (ENABLE_EPOLL) {
            log.info("netty use epoll");
        }
        EventLoopGroup[] groups = new EventLoopGroup[2];
        groups[0] = ENABLE_EPOLL ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        groups[1] = ENABLE_EPOLL ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(groups[0], groups[1])
                    .channel(ENABLE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new SocksServerInitializer(ENABLE_AUTH));
            log.info("socks server started at port {}", PORT);
            b.bind(PORT).sync().channel().closeFuture().sync();
        } finally {
            for (EventLoopGroup group : groups) {
                group.shutdownGracefully();
            }
        }
    }

}
