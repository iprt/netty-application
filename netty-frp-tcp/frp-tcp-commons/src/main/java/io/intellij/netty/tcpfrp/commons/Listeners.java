package io.intellij.netty.tcpfrp.commons;

import io.intellij.netty.tcpfrp.protocol.channel.FrpChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * ChannelFutureListeners
 *
 * @author tech@intellij.io
 * @since 2025-03-08
 */
@Slf4j
public class Listeners {
    private Listeners() {
    }

    public static @NotNull ChannelFutureListener read() {
        return f -> {
            if (f.isSuccess()) {
                f.channel().read();
            } else {
                log.error("read failure: ", f.cause());
            }
        };
    }

    public static @NotNull ChannelFutureListener read(Channel ch) {
        return read(ch, "");
    }

    public static @NotNull ChannelFutureListener read(Channel ch, String failureMessage) {
        return f -> {
            if (f.isSuccess()) {
                if (ch != null && ch.isActive()) {
                    ch.read();
                } else {
                    log.error("channel is null or not active, cannot read");
                }
            } else {
                log.error("read failure: {}", failureMessage, f.cause());
            }
        };
    }

    public static @NotNull ChannelFutureListener read(@NotNull FrpChannel frpChannel) {
        return read(frpChannel, "");
    }

    public static @NotNull ChannelFutureListener read(@NotNull FrpChannel frpChannel, String failureMessage) {
        return f -> {
            if (f.isSuccess()) {
                frpChannel.read();
            } else {
                log.error("read failure: {}", failureMessage, f.cause());
            }
        };
    }

    public static @NotNull ChannelFutureListener releaseDispatchChannel(String dispatchId) {
        return releaseDispatchChannel(dispatchId, "");
    }

    public static @NotNull ChannelFutureListener releaseDispatchChannel(String dispatchId, String failureMessage) {
        return f -> {
            if (f.isSuccess()) {
                DispatchManager.getInstance().release(dispatchId, failureMessage);
            } else {
                log.error("release failure: {}", failureMessage, f.cause());
            }
        };

    }

}
