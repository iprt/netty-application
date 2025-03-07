package io.intellij.netty.tcpfrp.protocol.codec;

/**
 * FrpEncoder
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
public abstract class FrpEncoder {

    public static FrpBasicMsgEncoder basicMsgEncoder() {
        return new FrpBasicMsgEncoder();
    }

    public static DispatchEncoder dispatchEncoder() {
        return new DispatchEncoder();
    }

}
