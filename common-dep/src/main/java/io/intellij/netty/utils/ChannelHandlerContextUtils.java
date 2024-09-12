package io.intellij.netty.utils;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * ChannelHandlerContextUtils
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ChannelHandlerContextUtils {
    private ChannelHandlerContextUtils() {
    }

    public static ConnHostPort getRemoteAddress(ChannelHandlerContext ctx) {
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            String remoteIp = remoteAddress.getAddress().getHostAddress();
            int remotePort = remoteAddress.getPort();
            return ConnHostPort.of(remoteIp, remotePort);
        } catch (Exception e) {
            log.error("", e);
            return ConnHostPort.unknown();
        }
    }
}
