package io.intellij.netty.tcpfrp.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;

/**
 * EventLoopGroupContainer
 *
 * @author tech@intellij.io
 */
@Getter
public class EventLoopGroupContainer {
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    private static final EventLoopGroupContainer instance = new EventLoopGroupContainer();

    private EventLoopGroupContainer() {
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public static EventLoopGroupContainer get() {
        return instance;
    }
}
