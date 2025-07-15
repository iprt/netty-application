package io.intellij.netty.server.socks.config;

/**
 * Environment
 *
 * @author tech@intellij.io
 */
public class Environment {

    public static final String SOCKS5_USERNAME = System.getenv("SOCKS5_USERNAME");

    public static final String SOCKS5_PASSWORD = System.getenv("SOCKS5_PASSWORD");

}
