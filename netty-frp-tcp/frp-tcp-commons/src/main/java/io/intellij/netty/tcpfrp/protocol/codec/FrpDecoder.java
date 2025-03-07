package io.intellij.netty.tcpfrp.protocol.codec;

import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * FrpDecoder
 *
 * @author tech@intellij.io
 * @since 2025-03-07
 */
public abstract class FrpDecoder extends ReplayingDecoder<FrpBasicMsg.State> {
    protected FrpDecoder(FrpBasicMsg.State initialState) {
        super(initialState);
    }

    public static FrpClientDecoder clientDecoder() {
        return new FrpClientDecoder();
    }

    public static FrpServerDecoder serverDecoder() {
        return new FrpServerDecoder();
    }

}
