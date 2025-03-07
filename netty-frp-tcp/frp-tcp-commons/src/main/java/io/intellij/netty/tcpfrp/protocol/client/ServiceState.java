package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.protocol.ConnState;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.intellij.netty.tcpfrp.protocol.ConnState.BROKEN;
import static io.intellij.netty.tcpfrp.protocol.ConnState.FAILURE;
import static io.intellij.netty.tcpfrp.protocol.ConnState.SUCCESS;

/**
 * ServiceConnState
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceState {
    /**
     * {@link ConnState}
     */
    private String stateName;
    private String dispatchId;

    public static FrpBasicMsg success(String dispatchId) {
        return FrpBasicMsg.createServiceState(
                new ServiceState(SUCCESS.getName(), dispatchId)
        );
    }

    public static FrpBasicMsg failure(String dispatchId) {
        return FrpBasicMsg.createServiceState(new ServiceState(FAILURE.getName(), dispatchId));
    }

    public static FrpBasicMsg broken(String dispatchId) {
        return FrpBasicMsg.createServiceState(
                new ServiceState(BROKEN.getName(), dispatchId)
        );
    }

}
