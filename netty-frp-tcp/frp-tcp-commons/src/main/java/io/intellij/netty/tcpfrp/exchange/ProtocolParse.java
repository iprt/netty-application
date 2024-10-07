package io.intellij.netty.tcpfrp.exchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ProtocolParse
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ProtocolParse<T> {
    private boolean valid;
    private String invalidMsg;
    private ExchangeType exchangeType;
    private T data;
}