package io.intellij.netty.tcpfrp.commons;

import io.intellij.netty.tcpfrp.protocol.channel.DispatchPacket;
import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final AtomicBoolean enableDispatch;

    private DispatchManager() {
        idToChannelMap = new ConcurrentHashMap<>();
        enableDispatch = new AtomicBoolean(true);
    }

    public void addChannel(String dispatchId, Channel channel) {
        if (!enableDispatch.get()) {
            throw new RuntimeException("DispatchManager is disabled");
        }
        idToChannelMap.put(dispatchId, channel);
    }

    public Channel getChannel(String dispatchId) {
        if (!enableDispatch.get()) {
            throw new RuntimeException("DispatchManager is disabled");
        }
        return idToChannelMap.get(dispatchId);
    }

    public void release(String dispatchId) {
        if (!enableDispatch.get()) {
            throw new RuntimeException("DispatchManager is disabled");
        }
        log.warn("release channel|dispatchId={}", dispatchId);
        ChannelUtils.close(idToChannelMap.remove(dispatchId));
    }

    public void release(String dispatchId, String reason) {
        if (!enableDispatch.get()) {
            throw new RuntimeException("DispatchManager is disabled");
        }
        log.warn("release channel|dispatchId={}|reason={}", dispatchId, reason);
        ChannelUtils.close(idToChannelMap.remove(dispatchId));
    }

    public void releaseAll() {
        enableDispatch.set(false);
        log.warn("release all channels");
        idToChannelMap.values().forEach(ChannelUtils::close);
        idToChannelMap.clear();
    }

    public void dispatch(DispatchPacket data, ChannelFutureListener... listeners) {
        Channel channel = getChannel(data.getDispatchId());
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(data.getPacket()).addListeners(listeners);
        } else {
            log.error("DispatchManager dispatch failed|dispatchId={}|channel={}", data.getDispatchId(), channel);
        }
    }

}
