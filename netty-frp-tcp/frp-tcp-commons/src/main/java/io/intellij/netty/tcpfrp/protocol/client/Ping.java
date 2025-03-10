package io.intellij.netty.tcpfrp.protocol.client;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Ping
 *
 * @author tech@intellij.io
 * @since 2025-03-10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ping {
    private Date time;
    private String name;

    public static FrpBasicMsg create(String name) {
        return FrpBasicMsg.createPing(new Ping(new Date(), name));
    }

}
