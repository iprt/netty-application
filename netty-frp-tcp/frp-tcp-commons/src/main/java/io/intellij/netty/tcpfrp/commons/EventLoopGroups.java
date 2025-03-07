package io.intellij.netty.tcpfrp.commons;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * EventLoopGroups
 *
 * @author tech@intellij.io
 */
public class EventLoopGroups {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private static final EventLoopGroups instance = new EventLoopGroups();

    private EventLoopGroups() {
        this.bossGroup = null;
        this.workerGroup = null;
    }

    public EventLoopGroup getBossGroup() {
        if (this.bossGroup == null) {
            this.bossGroup = new NioEventLoopGroup(1);
        }
        return this.bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        if (this.workerGroup == null) {
            this.workerGroup = new NioEventLoopGroup();
        }
        return this.workerGroup;
    }

    public static EventLoopGroups get() {
        return instance;
    }
}
