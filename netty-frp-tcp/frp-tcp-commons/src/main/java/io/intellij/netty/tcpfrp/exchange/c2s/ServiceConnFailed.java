package io.intellij.netty.tcpfrp.exchange.c2s;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ConnServiceResp
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class ServiceConnFailed {
    private String userChannelId;

    public static ServiceConnFailed create(String userChannelId) {
        return new ServiceConnFailed(userChannelId);
    }

}
