package io.intellij.netty.example.dispatch.protocol;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Msg
 *
 * @author tech@intellij.io
 */
@Getter
public class ProtocolMsg {
    private final ProtocolMsgType protocolMsgType;
    private final int len;
    private final String msgJson;

    private ProtocolMsg(ProtocolMsgType protocolMsgType, int len, String msgJson) {
        this.len = len;
        this.protocolMsgType = protocolMsgType;
        this.msgJson = msgJson;

        if (StringUtils.isBlank(msgJson)) {
            throw new IllegalArgumentException("msgJson is blank");
        }
    }

}
