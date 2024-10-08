package io.intellij.netty.tcpfrp.exchange.codec;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * ExchangeProtocol
 *
 * @author tech@intellij.io
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Data
public class ExchangeProtocol {
    private ExchangeType exchangeType;
    // for validate
    private String className;

    private byte[] body;
}
