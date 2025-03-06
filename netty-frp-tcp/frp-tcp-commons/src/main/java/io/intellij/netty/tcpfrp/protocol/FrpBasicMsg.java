package io.intellij.netty.tcpfrp.protocol;

import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.client.ServiceConnState;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.protocol.server.UserConnState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FrpBasicMsg
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FrpBasicMsg {

    public enum State {
        READ_TYPE,
        READ_LENGTH,
        READ_BASIC_MSG,
        READ_DATA_PACKET
    }

    private FrpMsgType msgType;
    private Object msgBody;

    static FrpBasicMsg create(FrpMsgType msgType, Object msgBody) {
        return new FrpBasicMsg(msgType, msgBody);
    }

    public static FrpBasicMsg createAuthRequest(AuthRequest authRequest) {
        return create(FrpMsgType.AUTH_REQUEST, authRequest);
    }

    public static FrpBasicMsg createAuthResponse(AuthResponse authResponse) {
        return create(FrpMsgType.AUTH_RESPONSE, authResponse);
    }

    public static FrpBasicMsg createListeningRequest(ListeningRequest listeningRequest) {
        return create(FrpMsgType.LISTENING_REQUEST, listeningRequest);
    }

    public static FrpBasicMsg createListeningResponse(ListeningResponse listeningResponse) {
        return create(FrpMsgType.LISTENING_RESPONSE, listeningResponse);
    }

    public static FrpBasicMsg createUserConnState(UserConnState userConnState) {
        return create(FrpMsgType.USER_CONN_STATE, userConnState);
    }

    public static FrpBasicMsg createServiceConnState(ServiceConnState serviceConnState) {
        return create(FrpMsgType.SERVICE_CONN_STATE, serviceConnState);
    }

}
