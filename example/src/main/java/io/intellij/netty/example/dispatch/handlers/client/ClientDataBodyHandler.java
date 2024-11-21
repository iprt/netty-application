package io.intellij.netty.example.dispatch.handlers.client;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.example.dispatch.model.DataBody;
import io.intellij.netty.example.dispatch.model.msg.Response;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * ClientDataBodyHandler
 *
 * @author tech@intellij.io
 */
@Slf4j
public class ClientDataBodyHandler extends SimpleChannelInboundHandler<DataBody> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DataBody msg) throws Exception {
        if (msg.getDataType() == 3) {
            ctx.fireChannelRead(JSON.parseObject(msg.getJson(), Response.class));
        } else {
            throw new RuntimeException("Unknown data type");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("DataBodyHandler error|{}", cause.getMessage());
        ctx.close();
    }
}
