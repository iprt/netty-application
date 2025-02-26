package io.intellij.netty.tcpfrp.exchange.codec;

import io.intellij.netty.tcpfrp.exchange.c2s.ListeningConfigReport;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceBreakConn;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnFailed;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceConnSuccess;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceDataPacket;
import io.intellij.netty.tcpfrp.exchange.s2c.ListeningLocalResp;
import io.intellij.netty.tcpfrp.exchange.s2c.UserBreakConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserCreateConn;
import io.intellij.netty.tcpfrp.exchange.s2c.UserDataPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Random;

/**
 * ExchangeType
 * <p>
 * C2S : frp-client send to frp-server
 * <p>
 * S2C:  frp-server send to frp-client
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Getter
public enum ExchangeType {

    /**
     * 客户端发送监听配置给服务端
     */
    C2S_SEND_CONFIG(0, ListeningConfigReport.class, "client send listening config to server"),

    /**
     * 服务端告知客户端监听的配置列表的回复
     */
    S2C_LISTENING_CONFIG_RESP(1, ListeningLocalResp.class, "server send try listening response "),

    // ---------- ---------- ---------- ---------- ----------

    /**
     * 服务端 获取到用户新建连接 (服务端连接事件 用户建立连接 e.g. user ---> frp-server:3306)
     */
    S2C_RECEIVE_USER_CONN_CREATE(2, UserCreateConn.class, "frp-server receive user's connection"),

    /**
     * 服务端 获取到用户断开连接 (服务端连接事件 用户断开连接 e.g. user -×-> frp-server:3306)
     */
    S2C_RECEIVE_USER_CONN_BREAK(3, UserBreakConn.class, "frp-server lost user's connection"),

    /**
     * 客户端连接真实服务成功    (客户端端连接事件 建立到真实服务的连接成功)
     */
    C2S_CONN_REAL_SERVICE_SUCCESS(4, ServiceConnSuccess.class, "frp-client connect to real server success"),

    /**
     * 客户端连接真实服务失败    (客户端端连接事件 建立到真实服务的连接失败 )
     */
    C2S_CONN_REAL_SERVICE_FAILED(5, ServiceConnFailed.class, "frp-client connect to real server failed"),

    /**
     * 客户端丢失真实服务的连接   (客户端端连接事件 运行中的服务断开了连接)
     * <p>
     * TODO 本质上否等价于 CLIENT_TO_SERVER_CONN_REAL_SERVICE_FAILED
     */
    C2S_LOST_REAL_SERVER_CONN(6, ServiceBreakConn.class, "frp-client lost real service's connection"),

    /**
     * 服务端接收到用户的数据
     */
    S2C_USER_DATA_PACKET(7, UserDataPacket.class, "frp server get user' data packet"),

    /**
     * 客户端接收到服务的数据
     */
    C2S_SERVICE_DATA_PACKET(8, ServiceDataPacket.class, "frp client get service' data packet"),
    ;

    private final int type;
    private final Class<?> clazz;
    private final String desc;

    private static final int TOTAL = ExchangeType.values().length;

    private static final Random R = new Random();

    public static ExchangeType parseType(int type) {
        for (ExchangeType exchangeType : ExchangeType.values()) {
            if (exchangeType.getType() == type) {
                return exchangeType;
            }
        }
        return null;
    }

    // [ -2^7 ] 至 [ 2^7 - 1 ]
    public static int decodeRandomToReal(int type) {
        return type % TOTAL;
    }

    public static int encodeRealToRandom(int type) {
        int random = R.nextInt(1, 10);
        return type + random * TOTAL;
    }

}
