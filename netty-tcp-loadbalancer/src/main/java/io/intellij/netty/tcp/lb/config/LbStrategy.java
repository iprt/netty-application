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
    LEAST_CONN,
    HASH;

    public static LbStrategy fromString(String strategy) {
        return switch (strategy) {
            case "round_robin" -> ROUND_ROBIN;
            case "least_conn" -> LEAST_CONN;
            case "hash" -> HASH;
            default -> RANDOM;
        };
    }
}
