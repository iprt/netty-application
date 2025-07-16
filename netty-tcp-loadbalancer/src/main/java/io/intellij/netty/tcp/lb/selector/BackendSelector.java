package io.intellij.netty.tcp.lb.selector;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.config.LbStrategy;
import io.intellij.netty.tcp.lb.selector.strategies.LeastConnSelector;
import io.intellij.netty.tcp.lb.selector.strategies.RandomSelector;
import io.intellij.netty.tcp.lb.selector.strategies.RoundRobinSelector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * BackendSelector
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
public interface BackendSelector {

    Backend select();

    Backend nextIfConnectFailed(Backend selected);

    void onChannelActive(Backend target);

    void onChannelInactive(Backend target);

    void reset();

    static @NotNull BackendSelector get(@NotNull LbStrategy strategy, Map<String, Backend> backends) {
        return switch (strategy) {
            case RANDOM -> new RandomSelector(backends);
            case ROUND_ROBIN -> new RoundRobinSelector(backends);
            case LEAST_CONN -> new LeastConnSelector(backends);
            default -> throw new IllegalStateException("Unexpected value: " + strategy);
        };

    }

}
