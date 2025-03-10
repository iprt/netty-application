package io.intellij.netty.tcpfrp.protocol.server;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * Pong
 *
 * @author tech@intellij.io
 * @since 2025-03-10
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Pong {
    private Date time;
    private String name;

    public static FrpBasicMsg create(String name) {
        return FrpBasicMsg.createPong(new Pong(new Date(), name));
    }
}
