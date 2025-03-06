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
    private String userId;
    private String serviceId;

    public static FrpBasicMsg connSuccess(String userId, String serviceId) {
        return FrpBasicMsg.createServiceConnState(
                new ServiceConnState(ConnState.SUCCESS.getName(), userId, serviceId)
        );
    }

    public static FrpBasicMsg connFailure(String userId) {
        return FrpBasicMsg.createServiceConnState(
                new ServiceConnState(ConnState.FAILURE.getName(), userId, null)
        );
    }

    public static FrpBasicMsg connBroken(String userId, String serviceId) {
        return FrpBasicMsg.createServiceConnState(
                new ServiceConnState(ConnState.BROKEN.getName(), userId, serviceId)
        );
    }

}
