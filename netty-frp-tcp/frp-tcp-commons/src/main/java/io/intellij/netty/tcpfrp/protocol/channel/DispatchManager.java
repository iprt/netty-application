package io.intellij.netty.tcpfrp.protocol.channel;

import io.intellij.netty.utils.ChannelUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

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

    private static final AttributeKey<DispatchManager> DISPATCH_MANAGER_KEY = AttributeKey.valueOf("dispatch_manager");

    public static void buildIn(@NotNull Channel channel) {
        channel.attr(DISPATCH_MANAGER_KEY).set(new DispatchManager());
    }

    public static @NotNull DispatchManager getBy(Channel channel) {
        DispatchManager dispatchManager = channel.attr(DISPATCH_MANAGER_KEY).get();
        if (dispatchManager == null) {
            throw new RuntimeException("DispatchManager is not initialized");
        }
        return dispatchManager;
    }

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
        log.warn("[Release] release channel|dispatchId={}", dispatchId);
        ChannelUtils.close(idToChannelMap.remove(dispatchId));
    }

    public void release(String dispatchId, String reason) {
        if (!enableDispatch.get()) {
            throw new RuntimeException("DispatchManager is disabled");
        }
        log.warn("[Release] release channel|dispatchId={}|reason={}", dispatchId, reason);
        ChannelUtils.close(idToChannelMap.remove(dispatchId));
    }

    public void releaseAll() {
        enableDispatch.set(false);
        log.warn("[Release] release all dispatch channels");
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
