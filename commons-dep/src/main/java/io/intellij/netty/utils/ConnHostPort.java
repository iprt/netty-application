package io.intellij.netty.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * ConnHostPort
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor
@ToString
@Getter
public class ConnHostPort {
    private final String host;
    private final int port;

    public static ConnHostPort of(String host, int port) {
        return new ConnHostPort(host, port);
    }

    public static ConnHostPort unknown() {
        return of("unknown", 0);
    }

}
