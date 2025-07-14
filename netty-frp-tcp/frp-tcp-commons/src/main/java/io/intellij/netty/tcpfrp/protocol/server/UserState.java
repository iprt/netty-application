package io.intellij.netty.tcpfrp.protocol.server;

import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static io.intellij.netty.tcpfrp.protocol.ConnState.ACCEPT;
import static io.intellij.netty.tcpfrp.protocol.ConnState.BROKEN;
import static io.intellij.netty.tcpfrp.protocol.ConnState.READY;

/**
 * UserState
 * <p>
 * frp-server 发送给 frp-client 的用户连接状态
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserState {
    /**
     * {@link ConnState}
     */
    private String stateName;
    private String dispatchId;
    private Integer listeningPort;

    @Contract("_, _ -> new")
    public static @NotNull FrpBasicMsg accept(String dispatchId, int listeningPort) {
        return FrpBasicMsg.createUserState(
                new UserState(ACCEPT.getName(), dispatchId, listeningPort)
        );
    }

    @Contract("_ -> new")
    public static @NotNull FrpBasicMsg ready(String dispatchId) {
        return FrpBasicMsg.createUserState(
                new UserState(READY.getName(), dispatchId, null)
        );
    }

    @Contract("_ -> new")
    public static @NotNull FrpBasicMsg broken(String dispatchId) {
        return FrpBasicMsg.createUserState(
                new UserState(BROKEN.getName(), dispatchId, null)
        );
    }

}
