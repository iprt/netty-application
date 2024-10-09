package io.intellij.netty.tcpfrp.exchange.codec;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceDataPacket;
import io.intellij.netty.tcpfrp.exchange.s2c.UserDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ExchangeProtocolUtils
 *
 * @author tech@intellij.io
 */
public class ExchangeProtocolUtils {
    private ExchangeProtocolUtils() {
    }

    public static ExchangeProtocol buildProtocolByJson(ExchangeType exchangeType, Object obj) {
        if (Objects.isNull(exchangeType) || Objects.isNull(obj)) {
            return null;
        }
        return new ExchangeProtocol(exchangeType, exchangeType.getClazz().getName(), JSON.toJSONBytes(obj));
    }

    public static <T> ProtocolParse<T> parseProtocolByJson(@NotNull ExchangeProtocol msg, @NotNull Class<T> target) {
        ExchangeType exchangeType = msg.exchangeType();
        String protocolClassName = exchangeType.getClazz().getName();
        String targetClassName = target.getName();
        if (exchangeType.getClazz() == ServiceDataPacket.class || exchangeType.getClazz() == UserDataPacket.class || protocolClassName.equals(targetClassName)) {
            try {
                T obj = JSON.parseObject(msg.body(), target);
                return Objects.isNull(obj) ? ProtocolParse.failed("JSON.parseObject(json, target) return null|type=" + exchangeType)
                        : ProtocolParse.success(exchangeType, obj);
            } catch (Exception e) {
                return ProtocolParse.failed(e.getMessage());
            }
        } else {
            return ProtocolParse.failed(
                    String.format("msg's classname does not match target's classname|protocol.classname=%s|target.classname=%s", protocolClassName, targetClassName)
            );
        }
    }

}
