package io.intellij.netty.tcp.lb.config;

import lombok.Data;

/**
 * RemoteServer
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@Data
public class Backend {
    private String name;
    private String host;
    private int port;

    public String detail() {
        return "name:" + name + "|host:" + host + "|port:" + port;
    }
}
