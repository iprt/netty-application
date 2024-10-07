package io.intellij.netty.tcpfrp.exchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ExchangeType
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Getter
public enum ExchangeType {

    /**
     * 客户端发送监听配置给服务端
     */
    CLIENT_TO_SERVER_SEND_CONFIG(1, "client send listening config to server"),


    /**
     * 服务端告知客户端监听的配置列表的回复
     */
    SERVER_TO_CLIENT_CONFIG_RESP(2, "server send try conn resp"),

    // ---------- ---------- ---------- ---------- ----------

    /**
     * 服务端 获取到用户新建连接 (服务端连接事件 用户建立连接)
     */
    SERVER_TO_CLIENT_RECEIVE_USER_CONN_CREATE(3, "frp-server receive user's connection"),

    /**
     * 服务端 获取到用户断开连接 (服务端连接事件 用户断开连接)
     */
    SERVER_TO_CLIENT_RECEIVE_USER_CONN_BREAK(4, "frp-server lost user's connection"),

    /**
     * 客户端连接真实服务成功    (客户端端连接事件 建立到服务的连接成功)
     */
    CLIENT_TO_SERVER_CONN_REAL_SERVICE_SUCCESS(5, "frp-client connect to real server success"),

    /**
     * 客户端连接真实服务失败     (客户端端连接事件 建立到服务的连接失败)
     */
    CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED(6, "frp-client connect to real server failed"),

    /**
     * 客户端丢失真实服务的连接   (客户端端连接事件 运行中的服务断开了连接)
     */
    CLIENT_TO_SERVER_LOST_REAL_SERVER_CONN(7, "frp-client lost real service's connection"),


    /**
     * 服务端接收到用户的数据
     */
    SERVER_TO_CLIENT_GET_USER_DATA(100, "frp server get user data"),

    /**
     * 客户端接收到服务的数据
     */
    CLIENT_TO_SERVER_GET_SERVICE_DATA(200, "frp client get service data"),
    ;

    private final int type;
    private final String desc;

    public static ExchangeType getType(int value) {
        for (ExchangeType exchangeType : ExchangeType.values()) {
            if (exchangeType.getType() == value) {
                return exchangeType;
            }
        }
        return null;
    }

}
