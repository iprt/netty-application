package io.intellij.netty.server.socks.handlers.socks5auth;

/**
 * AuthenticateResponse
 *
 * @author tech@intellij.io
 */
public record AuthenticateResponse(boolean success, String message) {
}
