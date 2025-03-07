package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AuthToken
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthRequest {
    private String token;

    public static FrpBasicMsg create(String token) {
        return FrpBasicMsg.createAuthRequest(new AuthRequest(token));
    }

}
