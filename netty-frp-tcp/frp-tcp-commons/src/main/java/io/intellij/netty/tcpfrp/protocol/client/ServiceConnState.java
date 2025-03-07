package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ServiceConnState
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceConnState {
    private String connState;
    private String dispatchId;

    public static FrpBasicMsg connSuccess(String dispatchId) {
        return FrpBasicMsg.createServiceConnState(
                new ServiceConnState(ConnState.SUCCESS.getName(), dispatchId)
        );
    }

    public static FrpBasicMsg connFailure(String dispatchId) {
        return FrpBasicMsg.createServiceConnState(
                new ServiceConnState(ConnState.FAILURE.getName(), dispatchId)
        );
    }

    public static FrpBasicMsg connBroken(String dispatchId) {
        return FrpBasicMsg.createServiceConnState(
                new ServiceConnState(ConnState.BROKEN.getName(), dispatchId)
        );
    }

}
