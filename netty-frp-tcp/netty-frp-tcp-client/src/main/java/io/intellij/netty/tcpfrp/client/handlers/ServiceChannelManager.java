package io.intellij.netty.tcpfrp.client.handlers;

import io.intellij.netty.tcpfrp.protocol.channel.DataPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ServiceChannelManager
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
@Slf4j
public class ServiceChannelManager {

    @Getter
    private static final ServiceChannelManager instance = new ServiceChannelManager();

    private final Map<String, Channel> userId2ServiceChannel;

    private ServiceChannelManager() {
        userId2ServiceChannel = new ConcurrentHashMap<>();
    }

    public void addChannel(String userId, Channel channel) {
        userId2ServiceChannel.put(userId, channel);
    }

    public Channel getChannel(String userId) {
        return userId2ServiceChannel.get(userId);
    }

    public void release(String userId) {
        log.warn("release service channel |userId={}", userId);
        Channel channel = userId2ServiceChannel.remove(userId);
        ChannelUtils.close(channel);
    }

    public void releaseAll() {
        log.warn("release all service channels");
        userId2ServiceChannel.values().forEach(ChannelUtils::close);
        userId2ServiceChannel.clear();
    }

    public ChannelFuture dispatch(DataPacket data) {
        Channel channel = getChannel(data.getUserId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(data.getPacket());
        } else {
            // 可能是frp server 关闭了连接
            log.error("ServiceChannelManager dispatch failed |serviceId={} |channel={}", data.getServiceId(), channel);
            return null;
        }
    }

}
