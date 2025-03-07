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

    private final Map<String, Channel> serviceId2Channel;

    private ServiceChannelManager() {
        serviceId2Channel = new ConcurrentHashMap<>();
    }

    public void addChannel(String serviceId, Channel channel) {
        serviceId2Channel.put(serviceId, channel);
    }

    public Channel getChannel(String serviceId) {
        return serviceId2Channel.get(serviceId);
    }

    public void release(String serviceId) {
        log.warn("release service channel |serviceId={}", serviceId);
        Channel channel = serviceId2Channel.remove(serviceId);
        ChannelUtils.close(channel);
    }

    public void releaseAll() {
        log.warn("release all service channels");
        serviceId2Channel.values().forEach(ChannelUtils::close);
        serviceId2Channel.clear();
    }

    public ChannelFuture dispatch(DataPacket data) {
        Channel channel = getChannel(data.getServiceId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(data.getPacket());
        } else {
            // 可能是frp server 关闭了连接
            log.error("ServiceChannelManager dispatch failed |serviceId={} |channel={}", data.getServiceId(), channel);
            return null;
        }
    }

}
