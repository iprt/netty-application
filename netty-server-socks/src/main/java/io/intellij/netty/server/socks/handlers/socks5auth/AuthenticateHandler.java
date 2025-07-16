package io.intellij.netty.server.socks.handlers.socks5auth;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequest;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AuthenticateHandler
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Slf4j
public class AuthenticateHandler extends SimpleChannelInboundHandler<Socks5PasswordAuthRequest> {

    private final Authenticator authenticator;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Socks5PasswordAuthRequest authRequest) throws Exception {
        String username = authRequest.username();
        String password = authRequest.password();
        AuthenticateResponse authenticateResponse = authenticator.authenticate(username, password);
        if (authenticateResponse.success()) {
            ctx.pipeline().remove(Socks5PasswordAuthRequestDecoder.class);
            ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
            ctx.pipeline().remove(this);
            ctx.writeAndFlush(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS));
        } else {
            log.error("Authentication failed: {}", authenticateResponse.message());
            ctx.pipeline().remove(this);
            ctx.writeAndFlush(new DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.FAILURE))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

}
