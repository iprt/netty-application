package io.intellij.netty.server.socks.handlers.socks5auth;

/**
 * Authentication
 *
 * @author tech@intellij.io
 */
public interface Authentication {

    boolean isAuthConfigured();

    boolean authenticate(String username, String password);

}
