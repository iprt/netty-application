package io.intellij.netty.utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;

/**
 * ChannelUtils
 *
 * @author tech@intellij.io
 */
public class ChannelUtils {
    public static void closeOnFlush(Channel ch) {
        if (ch != null && ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
    public static void close(Channel ch){
        if (ch != null && ch.isActive()) {
            ch.close();
        }
    }

    // 工具方法：打印 pipeline 链上的所有 handler 名称
    public static void printPipeline(ChannelPipeline pipeline) {
        StringBuilder sb = new StringBuilder("Pipeline chain: ");
        for (String name : pipeline.names()) {
            sb.append(name).append(" -> ");
        }
        // 去掉最后一个箭头并输出
        if (sb.lastIndexOf(" -> ") == sb.length() - 4) {
            sb.delete(sb.length() - 4, sb.length());
        }
        System.out.println(sb);
    }


}
