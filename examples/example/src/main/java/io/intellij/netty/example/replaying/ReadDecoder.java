package io.intellij.netty.example.replaying;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * ReadDecoder
 *
 * @author tech@intellij.io
 * @since 2025-03-02
 */
public class ReadDecoder extends ReplayingDecoder<ReadDecoder.State> {
    // 定义解码器状态：读取消息长度和消息内容
    enum State {
        READ_LENGTH,
        READ_CONTENT
    }

    private int length; // 保存读取到的消息长度

    // 构造函数指定初始状态为 READ_LENGTH
    public ReadDecoder() {
        super(State.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (state()) {
            case READ_LENGTH:
                // 假设前4个字节表示消息长度
                length = in.readInt();
                // 设置检查点，同时切换到读取消息内容的状态
                checkpoint(State.READ_CONTENT);
                // 故意没有 break，直接进入下一个状态解析内容
            case READ_CONTENT:
                // 读取指定长度的消息内容
                ByteBuf content = in.readBytes(length);
                // 将解码后的数据放入输出列表
                out.add(content);
                // 解码成功后，重置状态为 READ_LENGTH，并设置新的检查点
                checkpoint(State.READ_LENGTH);
                break;
            default:
                throw new IllegalStateException("无效的状态: " + state());
        }

    }

}
