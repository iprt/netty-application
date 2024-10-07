package io.intellij.netty.utils;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * CtxUtils
 *
 * @author tech@intellij.io
 */
@Slf4j
public class CtxUtils {
    private CtxUtils() {
    }

    public static ConnHostPort getRemoteAddress(ChannelHandlerContext ctx) {
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String remoteIp = remoteAddress.getAddress().getHostAddress();
            int remotePort = remoteAddress.getPort();
            return ConnHostPort.of(remoteIp, remotePort);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ConnHostPort.unknown();
        }
    }

    public static ConnHostPort getLocalAddress(ChannelHandlerContext ctx) {
        try {
            InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
            String localIp = localAddress.getAddress().getHostAddress();
            int localPort = localAddress.getPort();
            return ConnHostPort.of(localIp, localPort);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ConnHostPort.unknown();
        }
    }

    public static String getChannelId(ChannelHandlerContext ctx) {
        return ctx.channel().id().asLongText();
    }

}
