package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * AuthToken
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class AuthRequest {
    private String token;

    public boolean validateToken(String requestToken) {
        if (this.token == null) {
            return false;
        }
        return this.token.equals(requestToken);
    }

    public static FrpBasicMsg create(String token) {
        return FrpBasicMsg.createAuthRequest(new AuthRequest(token));
    }

}
