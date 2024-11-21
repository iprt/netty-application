package io.intellij.netty.example.dispatch.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ProtocolMsgType
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Getter
public enum ProtocolMsgType {
    HEARTBEAT(1, "心跳"),
    DATA(2, "数据"),
    ;

    private final int type;
    private final String desc;

    public static ProtocolMsgType get(int type) {
        for (ProtocolMsgType value : values()) {
            if (value.type == type) {
                return value;
            }
        }
        return null;
    }
}
