package io.intellij.netty.server.socks.handlers;

import io.intellij.netty.server.socks.handlers.connect.Socks4ServerConnectHandler;
import io.intellij.netty.server.socks.handlers.connect.Socks5ServerConnectHandler;
import io.intellij.netty.server.socks.handlers.socks5auth.AuthenticateHandler;
import io.intellij.netty.server.socks.handlers.socks5auth.Authenticator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandType;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5CommandType;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
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

    private final Authenticator authenticator;

    private SocksServerHandler(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public static SocksServerHandler getInstance(Authenticator authenticator) {
        if (INSTANCE == null) {
            synchronized (SocksServerHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SocksServerHandler(authenticator);
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
                    ctx.pipeline().remove(this);
                    ctx.pipeline().addLast(new Socks4ServerConnectHandler());
                    ctx.fireChannelRead(socksV4CmdRequest);
                } else {
                    log.error("Unsupported SOCKS4 command type: {}", socksV4CmdRequest.type());
                    ctx.close();
                }
                break;
            case SOCKS5:
                if (socksRequest instanceof Socks5InitialRequest) {
                    if (authenticator.isAuthConfigured()) {
                        ChannelPipeline pipeline = ctx.pipeline();
                        pipeline.addFirst(new Socks5PasswordAuthRequestDecoder());
                        pipeline.addLast(new AuthenticateHandler(authenticator));
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.PASSWORD));
                    } else {
                        ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
                        ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
                    }
                } else if (socksRequest instanceof Socks5PasswordAuthRequest authRequest) {
                    // AuthenticateHandler
                    ctx.fireChannelRead(authRequest);
                } else if (socksRequest instanceof Socks5CommandRequest socks5CommandRequest) {
                    if (socks5CommandRequest.type() == Socks5CommandType.CONNECT) {
                        // 理解链表的特性
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
