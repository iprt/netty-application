package io.intellij.netty.tcpfrp.exchange.c2s;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@Data
public class ServiceConnFailed {
    private boolean success;
    private String serviceChannelId;
    // user -> frp-server channelId
    private String userChannelId;
}
