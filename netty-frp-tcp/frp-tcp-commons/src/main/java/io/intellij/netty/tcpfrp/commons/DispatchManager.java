package io.intellij.netty.tcpfrp.commons;

import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DispatchManager
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
@Slf4j
public class DispatchManager {
    @Getter
    private static final DispatchManager instance = new DispatchManager();

    /**
     * dispatch id -> channel
     */
    private final Map<String, Channel> idToChannelMap;

    private DispatchManager() {
        idToChannelMap = new ConcurrentHashMap<>();
    }

    public void addChannel(String dispatchId, Channel channel) {
        idToChannelMap.put(dispatchId, channel);
    }

    public Channel getChannel(String dispatchId) {
        return idToChannelMap.get(dispatchId);
    }

    public void release(String dispatchId) {
        log.warn("release channel|dispatchId={}", dispatchId);
        ChannelUtils.close(idToChannelMap.remove(dispatchId));
    }

    public void release(String dispatchId, String reason) {
        log.warn("release channel|dispatchId={}|reason={}", dispatchId, reason);
        ChannelUtils.close(idToChannelMap.remove(dispatchId));
    }

    public void releaseAll() {
        log.warn("release all channels");
        idToChannelMap.values().forEach(ChannelUtils::close);
        idToChannelMap.clear();
    }

    public void initiativeChannelRead(String dispatchId) {
        Channel channel = idToChannelMap.get(dispatchId);
        if (channel != null && channel.isActive()) {
            // AUTO_READ = false
            channel.read();
        }
    }

    public ChannelFuture dispatch(DispatchPacket data) {
        Channel channel = getChannel(data.getDispatchId());
        if (channel != null && channel.isActive()) {
            return channel.writeAndFlush(data.getPacket());
        } else {
            // 可能是frp server 关闭了连接
            log.error("DispatchManager dispatch failed|dispatchId={}|channel={}", data.getDispatchId(), channel);
            return null;
        }
    }

}
