package io.intellij.netty.tcpfrp.protocol;

import io.intellij.netty.tcpfrp.protocol.client.AuthRequest;
import io.intellij.netty.tcpfrp.protocol.client.ListeningRequest;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Ping;
import io.intellij.netty.tcpfrp.protocol.client.ServiceState;
import io.intellij.netty.tcpfrp.protocol.server.AuthResponse;
import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.tcpfrp.protocol.heartbeat.Pong;
import io.intellij.netty.tcpfrp.protocol.server.UserState;
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
        READ_DISPATCH_PACKET
    }

    private FrpMsgType msgType;
    private Object msgBody;

    static FrpBasicMsg create(FrpMsgType msgType, Object msgBody) {
        return new FrpBasicMsg(msgType, msgBody);
    }

    /**
     * Creates an authentication request message with the provided authentication request data.
     *
     * @param authRequest the authentication request containing necessary authentication details
     * @return an instance of {@code FrpBasicMsg} representing the authentication request message
     */
    public static FrpBasicMsg createAuthRequest(AuthRequest authRequest) {
        return create(FrpMsgType.AUTH_REQUEST, authRequest);
    }

    /**
     * Creates an authentication response message with the provided authentication response data.
     *
     * @param authResponse the authentication response containing the result of the authentication attempt
     * @return an instance of {@code FrpBasicMsg} representing the authentication response message
     */
    public static FrpBasicMsg createAuthResponse(AuthResponse authResponse) {
        return create(FrpMsgType.AUTH_RESPONSE, authResponse);
    }

    /**
     * Creates a listening request message with the provided {@code ListeningRequest} data.
     *
     * @param listeningRequest the listening request containing the necessary data for configuring listening ports
     * @return an instance of {@code FrpBasicMsg} representing the listening request message
     */
    public static FrpBasicMsg createListeningRequest(ListeningRequest listeningRequest) {
        return create(FrpMsgType.LISTENING_REQUEST, listeningRequest);
    }

    /**
     * Creates a listening response message with the provided {@code ListeningResponse} data.
     *
     * @param listeningResponse the listening response containing details about the success status,
     *                          failure reason, or listening status for various configurations
     * @return an instance of {@code FrpBasicMsg} representing the listening response message
     */
    public static FrpBasicMsg createListeningResponse(ListeningResponse listeningResponse) {
        return create(FrpMsgType.LISTENING_RESPONSE, listeningResponse);
    }

    /**
     * Creates a user connection state message with the provided {@code UserState} data.
     *
     * @param userState the user state containing details about the user's connection status, such as state name,
     *                  dispatch ID, and listening port
     * @return an instance of {@code FrpBasicMsg} representing the user connection state message
     */
    public static FrpBasicMsg createUserState(UserState userState) {
        return create(FrpMsgType.USER_STATE, userState);
    }

    /**
     * Creates a service connection state message with the provided {@code ServiceState} data.
     *
     * @param serviceState the service state containing details such as connection state name and dispatch ID
     * @return an instance of {@code FrpBasicMsg} representing the service connection state message
     */
    public static FrpBasicMsg createServiceState(ServiceState serviceState) {
        return create(FrpMsgType.SERVICE_STATE, serviceState);
    }


    /**
     * Creates a ping message with the provided {@code Ping} data.
     *
     * @param ping the ping object containing the details of the ping message
     * @return an instance of {@code FrpBasicMsg} representing the ping message
     */
    public static FrpBasicMsg createPing(Ping ping) {
        return create(FrpMsgType.PING, ping);
    }

    /**
     * Creates a Pong message with the provided {@code Pong} data.
     *
     * @param pong the Pong object containing the details of the pong message
     * @return an instance of {@code FrpBasicMsg} representing the pong message
     */
    public static FrpBasicMsg createPong(Pong pong) {
        return create(FrpMsgType.PONG, pong);
    }

}
