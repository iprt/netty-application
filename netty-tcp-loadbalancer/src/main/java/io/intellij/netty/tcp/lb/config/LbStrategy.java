package io.intellij.netty.tcp.lb.config;

/**
 * LbStrategy
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
public enum LbStrategy {

    ROUND_ROBIN,
    RANDOM,
    HASH;

    public static LbStrategy fromString(String strategy) {
        return switch (strategy) {
            case "round-robin" -> ROUND_ROBIN;
            case "hash" -> HASH;
            default -> RANDOM;
        };
    }
}
