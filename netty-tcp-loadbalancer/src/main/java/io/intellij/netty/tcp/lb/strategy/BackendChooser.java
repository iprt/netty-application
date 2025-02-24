package io.intellij.netty.tcp.lb.strategy;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.config.LbStrategy;
import io.intellij.netty.tcp.lb.strategy.chooser.RandomChooser;
import io.intellij.netty.tcp.lb.strategy.chooser.RoundRobinChooser;
import io.netty.channel.Channel;

import java.util.Map;

/**
 * BackendChooser
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
public interface BackendChooser {

    Backend receive(Channel inboundChannel);

    Backend connectFailed(Channel inboundChannel, Backend choose);

    void active(Channel inboundChannel, Backend target);

    void inactive(Channel inboundChannel, Backend target);

    static BackendChooser get(LbStrategy strategy, Map<String, Backend> backends) {
        return switch (strategy) {
            case RANDOM -> new RandomChooser(backends);
            case ROUND_ROBIN -> new RoundRobinChooser(backends);
            default -> throw new IllegalStateException("Unexpected value: " + strategy);
        };

    }

}
