package io.intellij.netty.tcpfrp.commons;

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
public class DispatchChannelManager {

    @Getter
    private static final DispatchChannelManager instance = new DispatchChannelManager();

    private final Map<String, Channel> dispatchChannelMap;

    private DispatchChannelManager() {
        dispatchChannelMap = new ConcurrentHashMap<>();
    }

    public void addChannel(String dispatchId, Channel channel) {
        dispatchChannelMap.put(dispatchId, channel);
    }

    public Channel getChannel(String dispatchId) {
        return dispatchChannelMap.get(dispatchId);
    }

    public void release(String dispatchId) {
        log.warn("release service channel |dispatchId={}", dispatchId);
        Channel channel = dispatchChannelMap.remove(dispatchId);
        ChannelUtils.close(channel);
    }

    public void releaseAll() {
        log.warn("release all service channels");
        dispatchChannelMap.values().forEach(ChannelUtils::close);
        dispatchChannelMap.clear();
    }

    public void initiativeChannelRead(String dispatchId) {
        Channel channel = dispatchChannelMap.get(dispatchId);
        if (channel != null && channel.isActive()) {
            // AUTO_READ = false
            channel.read();
        }
    }

    public ChannelFuture dispatch(DataPacket data) {
        Channel channel = getChannel(data.getDispatchId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(data.getPacket());
        } else {
            // 可能是frp server 关闭了连接
            log.error("ServiceChannelManager dispatch failed |dispatchId={} |channel={}", data.getDispatchId(), channel);
            return null;
        }
    }

}
