package io.intellij.netty.tcpfrp.protocol.server;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthResponse
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {
    private boolean success;

    public static FrpBasicMsg success() {
        return FrpBasicMsg.createAuthResponse(new AuthResponse(true));
    }

    public static FrpBasicMsg failure() {
        return FrpBasicMsg.createAuthResponse(new AuthResponse(false));
    }

}
