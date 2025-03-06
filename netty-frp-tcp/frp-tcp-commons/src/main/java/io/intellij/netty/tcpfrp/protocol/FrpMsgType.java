package io.intellij.netty.tcpfrp.protocol;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * FrpMsgType
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@RequiredArgsConstructor
@Getter
public enum FrpMsgType {

    /**
     * 客户端：认证请求
     */
    AUTH_REQUEST(0, "auth request"),

    /**
     * 服务端：认证回复
     */
    AUTH_RESPONSE(1, "auth response"),

    /**
     * 客户端 -> 服务端: 发送监听配置给服务端
     */
    LISTENING_REQUEST(2, "listening request"),

    /**
     * 服务端 -> 客户端: 告知客户端监听的配置列表的回复
     */
    LISTENING_RESPONSE(3, "listening response"),

    /**
     * 服务端 -> 客户端：
     * <p>
     * 用户连接请求 (连接事件 用户建立连接 e.g. user ---> frp-server:3306)
     * <p>
     * 用户连接断开 (连接事件 用户建立连接 e.g. user -×-> frp-server:3306)
     */
    USER_CONN_STATE(4, "user conn state"),

    /**
     * 客户端 -> 服务端
     * <p>
     * frp-client连接到真实服务成功：（e.g. frp-client ---> mysql:3306）
     * frp-client连接到真实服务失败：（e.g. frp-client -x-> mysql:3306）
     */
    SERVICE_CONN_STATE(5, "service conn state"),

    /**
     * 客户端&服务端: 数据包，本质上结构一样 (type|userId:serviceId|data)
     */
    DATA_PACKET(6, "data packet");

    private final int type;
    private final String desc;

    public static FrpMsgType getByType(int type) {
        for (FrpMsgType msgType : FrpMsgType.values()) {
            if (msgType.getType() == type) {
                return msgType;
            }
        }
        return null;
    }

}
