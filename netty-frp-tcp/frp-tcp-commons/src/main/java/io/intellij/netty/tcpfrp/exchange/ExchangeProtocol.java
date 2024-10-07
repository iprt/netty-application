package io.intellij.netty.tcpfrp.exchange;

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

    private int classLen;

    private String className;

    private int bodyLen;

    private byte[] body;

}
