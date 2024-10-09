package io.intellij.netty.tcpfrp.exchange.codec;

import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ProtocolUtils
 *
 * @author tech@intellij.io
 */
public class ExchangeProtocolUtils {
    public static final int FIXED_CHANNEL_ID_LEN = 60;

    public static ExchangeProtocol buildProtocolByJson(ExchangeType exchangeType, Object obj) {
        if (Objects.isNull(exchangeType) || Objects.isNull(obj)) {
            return null;
        }
        return new ExchangeProtocol(exchangeType, exchangeType.getClazz().getName(), JSON.toJSONBytes(obj));

    }

    public static <T> ProtocolParse<T> parseProtocolBy(@NotNull ExchangeProtocol msg, @NotNull Class<T> target) {
        ExchangeType exchangeType = msg.exchangeType();
        String protocolClassName = exchangeType.getClazz().getName();
        String targetClassName = target.getName();
        if (protocolClassName.equals(targetClassName)) {
            try {
                T obj = JSON.parseObject(msg.body(), target);
                if (Objects.isNull(obj)) {
                    return ProtocolParse.<T>builder()
                            .valid(false).invalidMsg("JSON.parseObject(json, target) return null|type=" + exchangeType)
                            .build();
                }

                return ProtocolParse.<T>builder().valid(true).exchangeType(msg.exchangeType()).data(obj).build();

            } catch (Exception e) {
                return ProtocolParse.<T>builder()
                        .valid(false).invalidMsg(e.getMessage())
                        .build();
            }

        } else {
            return ProtocolParse.<T>builder()
                    .valid(false).exchangeType(exchangeType)
                    .invalidMsg(
                            String.format("msg's classname does not match target's classname|protocol.classname=%s|target.classname=%s",
                                    protocolClassName, targetClassName)
                    ).build();
        }
    }

}
