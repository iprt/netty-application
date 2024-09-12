package io.intellij.netty.server;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.utils.ChannelHandlerContextUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * PrintHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class PrintHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.info("ChannelActive    {}", JSON.toJSONString(ChannelHandlerContextUtils.getRemoteAddress(ctx)));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        if (bytes.length > 0) {
            log.info("STRING  |{}", printString(bytes));
            log.info("NUM(10) |{}", printNum(bytes));
            log.info("HEX(16) |{}", printHex(bytes));
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        log.info("ChannelInActive  {}", JSON.toJSONString(ChannelHandlerContextUtils.getRemoteAddress(ctx)));
    }

    static String printString(byte[] bytes) {
        return new String(bytes, CharsetUtil.UTF_8);
    }

    static String printNum(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(b & 0xFF).append(" ");
        }
        return result.toString().trim();
    }

    static String printHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(byteToHex(b)).append(" ");
        }
        return result.toString().trim();
    }

    static String byteToHex(byte num) {
        // 使用Integer.toHexString方法将字节转化为16进制
        // 并且将结果填充为2个字符长度
        String hex = Integer.toHexString(num & 0xFF);
        if (hex.length() < 2) {
            hex = '0' + hex;
        }
        return hex;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("ExceptionCaught  |{}|{}", JSON.toJSONString(ChannelHandlerContextUtils.getRemoteAddress(ctx)), cause.getMessage());
    }

}
