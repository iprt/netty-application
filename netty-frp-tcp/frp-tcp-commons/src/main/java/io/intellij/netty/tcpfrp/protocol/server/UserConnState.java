package io.intellij.netty.tcpfrp.protocol.server;

import io.intellij.netty.tcpfrp.config.ListeningConfig;
import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserConnState
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserConnState {
    private String conState;
    private String userId;
    private ListeningConfig listeningConfig;

    public static FrpBasicMsg accept(String userId, ListeningConfig listeningConfig) {
        return FrpBasicMsg.createUserConnState(
                new UserConnState(ConnState.ACCEPT.getName(), userId, listeningConfig)
        );
    }

    public static FrpBasicMsg broken(String userId) {
        return FrpBasicMsg.createUserConnState(
                new UserConnState(ConnState.BROKEN.getName(), userId, null)
        );
    }

}
