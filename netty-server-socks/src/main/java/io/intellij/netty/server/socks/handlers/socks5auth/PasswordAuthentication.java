package io.intellij.netty.server.socks.handlers.socks5auth;

import lombok.extern.slf4j.Slf4j;

import static io.intellij.netty.server.socks.config.Environment.SOCKS5_PASSWORD;
import static io.intellij.netty.server.socks.config.Environment.SOCKS5_USERNAME;

/**
 * PasswordAuthentication
 *
 * @author tech@intellij.io
 */
@Slf4j
public class PasswordAuthentication implements Authentication {
    private final boolean isAuthConfigured;

    public PasswordAuthentication() {
        this.isAuthConfigured = SOCKS5_USERNAME != null && !SOCKS5_USERNAME.isEmpty() &&
                SOCKS5_PASSWORD != null && !SOCKS5_PASSWORD.isEmpty();
        log.info("password authentication configured : {}", isAuthConfigured);
    }

    @Override
    public boolean isAuthConfigured() {
        return this.isAuthConfigured;
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (this.isAuthConfigured) {
            return SOCKS5_USERNAME.equals(username) && SOCKS5_PASSWORD.equals(password);
        } else {
            return true;
        }
    }

}
