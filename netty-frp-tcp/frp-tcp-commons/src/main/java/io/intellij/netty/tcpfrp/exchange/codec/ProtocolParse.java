package io.intellij.netty.tcpfrp.exchange.codec;

/**
 * ProtocolParse
 *
 * @author tech@intellij.io
 */
public record ProtocolParse<T>(boolean valid, String invalidMsg, ExchangeType exchangeType, T data) {

    public static <T> ProtocolParse<T> success(ExchangeType exchangeType, T data) {
        return new ProtocolParse<>(true, "", exchangeType, data);
    }

    public static <T> ProtocolParse<T> failed(String invalidMsg) {
        return new ProtocolParse<>(false, invalidMsg, null, null);
    }

}
