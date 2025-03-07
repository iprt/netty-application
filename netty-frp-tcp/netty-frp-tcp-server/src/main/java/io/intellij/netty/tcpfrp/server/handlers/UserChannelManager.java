package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserChannelManager
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
@Slf4j
public class UserChannelManager {
    @Getter
    private static final UserChannelManager instance = new UserChannelManager();

    /**
     * 用户ID - 用户的Channel
     */
    private final Map<String, Channel> dispatchChannelMap;

    private UserChannelManager() {
        this.dispatchChannelMap = new ConcurrentHashMap<>();
    }

    public void addChannel(String dispatchId, Channel channel) {
        dispatchChannelMap.put(dispatchId, channel);
    }

    public Channel getChannel(String dispatchId) {
        return dispatchChannelMap.get(dispatchId);
    }

    public void initiativeChannelRead(String dispatchId) {
        Channel channel = dispatchChannelMap.get(dispatchId);
        if (channel != null && channel.isActive()) {
            // AUTO_READ = false
            channel.read();
        }
    }

    public void release(String dispatchId) {
        log.warn("release user channel|dispatchId={}", dispatchId);
        Channel ch = dispatchChannelMap.remove(dispatchId);
        ChannelUtils.close(ch);
    }

    public void releaseAll() {
        log.warn("release all user channels");
        dispatchChannelMap.values().forEach(ChannelUtils::close);
        dispatchChannelMap.clear();
    }

    public ChannelFuture dispatch(@NotNull DataPacket dataPacket) {
        String dispatchId = dataPacket.getDispatchId();
        Channel channel = getChannel(dispatchId);
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(dataPacket.getPacket());
        } else {
            log.warn("UserChannelHandler dispatch error| dispatchId={} |channel={}", dispatchId, channel);
            return null;
        }
    }

}
