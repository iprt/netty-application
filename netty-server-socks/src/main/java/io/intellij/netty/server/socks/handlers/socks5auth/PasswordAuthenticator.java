package io.intellij.netty.server.socks.handlers.socks5auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static io.intellij.netty.server.socks.config.Environment.SOCKS5_PASSWORD;
import static io.intellij.netty.server.socks.config.Environment.SOCKS5_USERNAME;

/**
 * PasswordAuthenticator
 *
 * @author tech@intellij.io
 */
@Slf4j
public class PasswordAuthenticator implements Authenticator {
    private final boolean isAuthConfigured;

    public PasswordAuthenticator() {
        log.info("Retrieving Socks5 username from environment variable 'SOCKS5_USERNAME'");
        log.info("Retrieving Socks5 password from environment variable 'SOCKS5_PASSWORD'");
        this.isAuthConfigured = SOCKS5_USERNAME != null && !SOCKS5_USERNAME.isEmpty() &&
                SOCKS5_PASSWORD != null && !SOCKS5_PASSWORD.isEmpty();
        log.info("Socks5 password authentication configured: {}", isAuthConfigured);
    }

    @Override
    public boolean isAuthConfigured() {
        return this.isAuthConfigured;
    }

    @Override
    public AuthenticateResponse authenticate(String username, String password) {
        if (this.isAuthConfigured) {
            if (StringUtils.isBlank(username)) {
                return new AuthenticateResponse(false, "username is required");
            }
            if (StringUtils.isBlank(password)) {
                return new AuthenticateResponse(false, "password is required");
            }
            if (SOCKS5_USERNAME.equals(username) && SOCKS5_PASSWORD.equals(password)) {
                return new AuthenticateResponse(true, "Authentication successful");
            } else {
                return new AuthenticateResponse(false, "Invalid username or password");
            }
        } else {
            return new AuthenticateResponse(true, "No authentication configured");
        }
    }

}
