package io.intellij.netty.example.dispatch.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DataType
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@Getter
public enum DataType {

    LOGIN(1, "login"),
    LOGOUT(2, "logout"),
    RESPONSE(3, "response");

    private final int code;
    private final String desc;

}
