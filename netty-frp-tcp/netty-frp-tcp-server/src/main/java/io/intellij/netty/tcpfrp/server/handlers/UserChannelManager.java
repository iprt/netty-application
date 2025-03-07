package io.intellij.netty.tcpfrp.server.handlers;

import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.AttributeKey;
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
     * 服务连接的唯一标识
     * <p>
     * set from notifyUserChannelRead
     */
    public static final AttributeKey<String> SERVICE_ID_KEY = AttributeKey.valueOf("serverId");

    /**
     * 用户ID - 用户的Channel
     */
    private final Map<String, Channel> userChannelMap;

    private UserChannelManager() {
        this.userChannelMap = new ConcurrentHashMap<>();
    }

    public void addChannel(String userId, Channel channel) {
        userChannelMap.put(userId, channel);
    }

    public Channel getChannel(String userId) {
        return userChannelMap.get(userId);
    }

    public <T> void setAttrThenChannelRead(String userId, AttributeKey<T> key, T value) {
        Channel channel = userChannelMap.get(userId);
        if (channel != null && channel.isActive()) {
            channel.attr(key).set(value);
            // AUTO_READ = false
            channel.read();
        }
    }

    public <T> T getAttrValue(String userId, AttributeKey<T> key) {
        Channel channel = userChannelMap.get(userId);
        return channel == null ? null : channel.attr(key).get();
    }

    public void release(String userId) {
        log.warn("release user channel|userId={}", userId);
        Channel ch = userChannelMap.remove(userId);
        ChannelUtils.close(ch);
    }

    public void releaseAll() {
        log.warn("release all user channels");
        userChannelMap.values().forEach(ChannelUtils::close);
        userChannelMap.clear();
    }

    public ChannelFuture dispatch(@NotNull DataPacket dataPacket) {
        Channel channel = getChannel(dataPacket.getUserId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(dataPacket.getPacket());
        } else {
            log.warn("UserChannelHandler dispatch error| userId: {}, channel: {}, isActive: {}", dataPacket.getUserId(), channel, channel != null && channel.isActive());
            return null;
        }
    }

}
