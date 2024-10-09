package io.intellij.netty.tcpfrp.exchange.codec;

/**
 * ExchangeProtocol
 *
 * @author tech@intellij.io
 */
public record ExchangeProtocol(ExchangeType exchangeType, String className, byte[] body) {
}
