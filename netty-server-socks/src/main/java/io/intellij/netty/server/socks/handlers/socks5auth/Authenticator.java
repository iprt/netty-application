package io.intellij.netty.server.socks.handlers.socks5auth;

/**
 * Authenticator
 *
 * @author tech@intellij.io
 */
public interface Authenticator {

    /**
     * Determines whether authentication is configured by checking if
     * the necessary credentials, such as username and password, are properly set.
     *
     * @return true if authentication credentials are configured; false otherwise
     */
    boolean isAuthConfigured();

    /**
     * Authenticates a user using the provided username and password credentials.
     *
     * @param username the username of the user attempting to authenticate
     * @param password the password of the user attempting to authenticate
     * @return an {@code AuthenticateResponse} object containing the authentication result:
     * whether the authentication was successful and a corresponding message
     */
    AuthenticateResponse authenticate(String username, String password);

}
