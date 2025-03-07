package io.intellij.netty.tcpfrp.protocol;

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

    ACCEPT("ACCEPT", "连接创建"),

    READY("READY", "连接准备就绪"),

    SUCCESS("SUCCESS", "连接成功"),

    FAILURE("FAILURE", "连接失败"),

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
