package io.intellij.netty.tcp.lb;

import com.alibaba.fastjson2.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

/**
 * HttpResponseHandler
 *
 * @author tech@intellij.io
 * @since 2025-02-24
 */
@RequiredArgsConstructor
public class HttpResponseHandler extends SimpleChannelInboundHandler<HttpObject> {
    private final int port;
    private final Lock lock;
    private final Condition condition;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject httpObject) throws Exception {
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
        if (httpObject instanceof HttpRequest req) {
            String msg = JSON.toJSONString(Map.of("date", date, "port", port));
            byte[] bytes = msg.getBytes();
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(bytes)
            );
            response.headers()
                    .set(CONTENT_TYPE, APPLICATION_JSON)
                    .set(CONTENT_LENGTH, Integer.toString(bytes.length));

            ctx.writeAndFlush(response)
                    .addListener(ChannelFutureListener.CLOSE);
            // 获取 req 的uri
            String uri = req.uri();
            if ("/shutdown".equals(uri)) {
                lock.lock();
                try {
                    condition.signalAll();
                } finally {
                    lock.unlock();
                }
            }
        }
    }


}

