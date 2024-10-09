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
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
public class ServiceConnSuccess {
    // user -> frp-server channelId
    private String userChannelId;

    private String serviceChannelId;

    public static ServiceConnSuccess create(String userChannelId, String serviceChannelId) {
        return new ServiceConnSuccess(userChannelId, serviceChannelId);
    }
}
