package io.intellij.netty.server.socks.handlers;

import io.intellij.netty.server.socks.handlers.connect.Socks4ServerConnectHandler;
import io.intellij.netty.server.socks.handlers.connect.Socks5ServerConnectHandler;
import io.intellij.netty.server.socks.handlers.socks5auth.Authentication;
import io.netty.channel.ChannelFutureListener;
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
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.utils.ChannelUtils.closeOnFlush;

/**
 * SocksServerHandler
 *
 * @author tech@intellij.io
 */
@ChannelHandler.Sharable
@Slf4j
public class SocksServerHandler extends SimpleChannelInboundHandler<SocksMessage> {
    private static volatile SocksServerHandler INSTANCE;

    private final Authentication authentication;

    private SocksServerHandler(Authentication authentication) {
        this.authentication = authentication;
    }

    public static SocksServerHandler getInstance(Authentication authentication) {
        if (INSTANCE == null) {
            synchronized (SocksServerHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SocksServerHandler(authentication);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, @NotNull SocksMessage socksRequest) throws Exception {
        switch (socksRequest.version()) {
            case SOCKS4a:
                Socks4CommandRequest socksV4CmdRequest = (Socks4CommandRequest) socksRequest;
                if (socksV4CmdRequest.type() == Socks4CommandType.CONNECT) {
                    ctx.pipeline().addLast(new Socks4ServerConnectHandler());
                    ctx.pipeline().remove(this);
                    ctx.fireChannelRead(socksV4CmdRequest);
                } else {
                    log.error("Unsupported SOCKS4 command type: {}", socksV4CmdRequest.type());
                    ctx.close();
                }
                break;
            case SOCKS5:
                if (socksRequest instanceof Socks5InitialRequest) {
                    if (authentication.isAuthConfigured()) {
                        ctx.pipeline().addFirst(new Socks5PasswordAuthRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD));
                    } else {
                        ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                    }
                } else if (socksRequest instanceof Socks5PasswordAuthRequest authRequest) {
                    String username = authRequest.username();
                    String password = authRequest.password();
                    if (authentication.authenticate(username, password)) {
                        // ctx.pipeline().remove(Socks5PasswordAuthRequestDecoder.class);
                        ctx.pipeline().removeFirst();
                        ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                        ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
                    } else {
                        log.error("Authentication failed for user: {}|password: {}", username, password);
                        ctx.write(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE))
                                .addListener(ChannelFutureListener.CLOSE);
                    }
                } else if (socksRequest instanceof Socks5CommandRequest socks5CommandRequest) {
                    if (socks5CommandRequest.type() == Socks5CommandType.CONNECT) {
                        ctx.pipeline().addLast(new Socks5ServerConnectHandler());
                        ctx.pipeline().remove(this);
                        ctx.fireChannelRead(socks5CommandRequest);
                    } else {
                        log.error("Unsupported SOCKS5 command type: {}", socks5CommandRequest.type());
                        ctx.close();
                    }
                }
                break;
            case UNKNOWN:
                log.error("unknown socks version");
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
        log.error("Exception caught in SocksServerHandler", throwable);
        closeOnFlush(ctx.channel());
    }

}
