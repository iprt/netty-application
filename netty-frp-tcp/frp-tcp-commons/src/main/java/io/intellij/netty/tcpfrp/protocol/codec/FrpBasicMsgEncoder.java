package io.intellij.netty.tcpfrp.protocol.codec;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.protocol.FrpBasicMsg;
import io.intellij.netty.tcpfrp.protocol.FrpMsgType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * FrpBasicMsgEncoder
 * <p>
 * 用来编码基础的消息
 * <p>
 * type|length|json
 * <p>
 * 1|4|json
 *
 * @author tech@intellij.io
 * @since 2025-03-05
 */
final class FrpBasicMsgEncoder extends MessageToByteEncoder<FrpBasicMsg> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FrpBasicMsg basicMsg, ByteBuf out) throws Exception {
        FrpMsgType msgType = basicMsg.getMsgType();
        out.writeByte(msgType.getType());

        Object msgBody = basicMsg.getMsgBody();
        String json = JSON.toJSONString(msgBody);
        byte[] bytes = json.getBytes();

        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }

}
