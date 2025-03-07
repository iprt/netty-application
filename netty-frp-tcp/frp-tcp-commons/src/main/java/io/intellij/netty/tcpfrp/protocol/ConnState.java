package io.intellij.netty.tcpfrp.protocol;

import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ConnState
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Getter
public enum ConnState {
    /**
     * frp-server ---> frp-client
     * <p>
     * {@link UserState#accept(String, int)}
     */
    ACCEPT("ACCEPT", "用户创建连接"),

    /**
     * frp-server ---> frp-client
     * <p>
     * {@link UserState#ready(String)}
     */
    READY("READY", "用户连接准备就绪"),

    /**
     * frp-client ---> frp-server
     * <p>
     * {@link ServiceState#success(String)}
     */
    SUCCESS("SUCCESS", "服务连接成功"),

    /**
     * frp-client ---> frp-server
     * /**
     * {@link ServiceState#failure(String)}
     */
    FAILURE("FAILURE", "服务连接失败"),

    /**
     * frp-client <==> frp-server
     * <p>
     * {@link UserState#broken(String)}
     * <p>
     * {@link ServiceState#broken(String)}
     */
    BROKEN("BROKEN", "连接断开");

    private final String name;
    private final String desc;

    public static ConnState getByName(String name) {
        for (ConnState connState : ConnState.values()) {
            if (connState.getName().equals(name)) {
                return connState;
            }
        }
        return null;
    }

}
