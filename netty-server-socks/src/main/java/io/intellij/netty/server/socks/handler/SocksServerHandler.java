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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@ChannelHandler.Sharable
@Slf4j
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {

    public static final SocksServerHandler INSTANCE = new SocksServerHandler(false);
    public static final SocksServerHandler INSTANCE_ENABLE_AUTH = new SocksServerHandler(true);

    private final static Map<String, String> USER_PASS_MAP = new HashMap<>();

    private final boolean enableAuth;

    private SocksServerHandler(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, SocksMessage socksRequest) throws Exception {
        switch (socksRequest.version()) {
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) socksRequest;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    ctx.pipeline().addLast(new SocksServerConnectHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksRequest);
                } else {
                    ctx.close();
                }
                break;
            case SOCKS5:
                if (socksRequest instanceof Socks5InitialRequest) {
                    // auth support example
                    // ctx.pipeline().addFirst(new Socks5PasswordAuthRequestDecoder());
                    // ctx.write(new DefaultSocks5AuthMethodResponse(Socks5AuthMethod.PASSWORD));
                    if (enableAuth) {
                        // this addFirst tis important
                        ctx.pipeline().addFirst(new Socks5PasswordAuthRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD));
                    } else {
                        ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                    }
                } else if (socksRequest instanceof Socks5PasswordAuthRequest socks5PasswordAuthRequest) {
                    String username = socks5PasswordAuthRequest.username();
                    if (StringUtil.isNullOrEmpty(username)) {
                        log.error("username is empty");
                        ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE));
                    } else if (!USER_PASS_MAP.containsKey(username)) {
                        log.error("username is not found | username = {}", username);
                        ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE));
                    } else if (!USER_PASS_MAP.get(username).equals(socks5PasswordAuthRequest.password())) {
                        log.error("password is not match | username = {}", username);
                        ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE));
                    } else {
                        // ctx.pipeline().remove(Socks5PasswordAuthRequestDecoder.class);
                        // removeFirst because we addFirst in the beginning
                        ctx.pipeline().removeFirst();

                        ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                        ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                    }
                } else if (socksRequest instanceof Socks5CommandRequest socks5CmdRequest) {
                    if (socks5CmdRequest.type() == Socks5CommandType.CONNECT) {
                        ctx.pipeline().addLast(new SocksServerConnectHandler());
                        ctx.pipeline().remove(this);
                        ctx.fireChannelRead(socksRequest);
                    } else {
                        ctx.close();
                    }
                } else {
                    ctx.close();
                }
                break;
            case UNKNOWN:
                ctx.close();
                break;
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        log.error("", throwable);
        SocksServerUtils.closeOnFlush(ctx.channel());
    }

    void initUserAndPass() {
        InputStream is = null;
        try {
            is = ClassLoader.getSystemResourceAsStream("user_pass.properties");

            Properties prop = new Properties();
            prop.load(is);
            prop.keySet().forEach(key -> {
                log.info("username {} & password {}", key, prop.getProperty(key.toString()));
                USER_PASS_MAP.put(key.toString(), prop.getProperty(key.toString()));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }

}
