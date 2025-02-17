package io.intellij.netty.tcpfrp.exchange.codec;

/**
 * ExchangeProtocol
 *
 * @author tech@intellij.io
 */
public record ExchangeProtocol(ExchangeType exchangeType, String className, byte[] body) {
    public static final int FIXED_CHANNEL_ID_LEN = 60;
}

/*

| 1byte | 4byte len | ... msg |

| type | length | body |

 */