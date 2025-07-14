package io.intellij.netty.tcpfrp.protocol.server;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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

    @Contract(" -> new")
    public static @NotNull FrpBasicMsg success() {
        return FrpBasicMsg.createAuthResponse(new AuthResponse(true));
    }

    @Contract(" -> new")
    public static @NotNull FrpBasicMsg failure() {
        return FrpBasicMsg.createAuthResponse(new AuthResponse(false));
    }

}
