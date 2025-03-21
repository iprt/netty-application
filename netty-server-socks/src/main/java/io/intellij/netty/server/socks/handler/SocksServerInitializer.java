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
package io.intellij.netty.server.socks.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.SocksPortUnificationServerHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.Getter;

public final class SocksServerInitializer extends ChannelInitializer<SocketChannel> {

    @Getter
    private final boolean enableAuth;

    private final SocksServerHandler socksServerHandler;

    public SocksServerInitializer(boolean enableAuth) {
        this.enableAuth = enableAuth;
        socksServerHandler = this.enableAuth ? SocksServerHandler.INSTANCE_ENABLE_AUTH : SocksServerHandler.INSTANCE;
        if (this.enableAuth) {
            socksServerHandler.initUserAndPass();
        }
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(
                new LoggingHandler(LogLevel.DEBUG),
                new SocksPortUnificationServerHandler(),
                socksServerHandler
        );
    }
}
