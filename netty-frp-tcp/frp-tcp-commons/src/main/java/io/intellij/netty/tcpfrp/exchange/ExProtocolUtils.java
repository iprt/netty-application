package io.intellij.netty.tcpfrp.exchange;

import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ProtocolUtils
 *
 * @author tech@intellij.io
 */
public class ExProtocolUtils {

    public static ExchangeProtocol jsonProtocol(ExchangeType type, Object obj) {
        if (Objects.isNull(obj)) {
            return null;
        }

        String className = obj.getClass().getName();
        int classLen = className.length();

        String json = JSON.toJSONString(obj);
        int jsonLen = json.length();

        return ExchangeProtocol.builder()
                .exchangeType(type)
                .classLen(classLen).className(className)
                .bodyLen(jsonLen).body(json.getBytes())
                .build();

    }

    public static <T> ProtocolParse<T> parseObj(@NotNull ExchangeProtocol msg, @NotNull Class<T> target) {
        ExchangeType exchangeType = msg.getExchangeType();
        String protocolClassName = exchangeType.getClazz().getName();
        String targetClassName = target.getName();
        if (protocolClassName.equals(targetClassName)) {
            String json = new String(msg.getBody());
            try {
                T obj = JSON.parseObject(json, target);

                if (Objects.isNull(obj)) {
                    return ProtocolParse.<T>builder()
                            .valid(false).invalidMsg(" JSON.parseObject(json, target) return null")
                            .build();
                }

                return ProtocolParse.<T>builder()
                        .valid(true).exchangeType(msg.getExchangeType())
                        .data(obj).build();

            } catch (Exception e) {
                return ProtocolParse.<T>builder()
                        .valid(false).invalidMsg(e.getMessage())
                        .build();
            }

        } else {
            return ProtocolParse.<T>builder()
                    .valid(false).invalidMsg(
                            String.format("msg's classname does not match target's classname|protocol.classname=%s|target.classname=%s",
                                    protocolClassName, targetClassName)
                    )
                    .build();
        }
    }


}
