package io.intellij.netty.example.dispatch.handlers.server;

import io.netty.util.AttributeKey;

/**
 * Attributes
 *
 * @author tech@intellij.io
 */
public class Attributes {
    public static final AttributeKey<String> LOGIN_USERNAME = AttributeKey.newInstance("loginUsername");
    public static final AttributeKey<String> LOGOUT_USERNAME = AttributeKey.newInstance("logoutUsername");
}
