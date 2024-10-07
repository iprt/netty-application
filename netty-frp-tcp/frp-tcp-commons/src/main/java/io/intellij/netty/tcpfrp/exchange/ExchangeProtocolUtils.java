package io.intellij.netty.tcpfrp.exchange;

import com.alibaba.fastjson2.JSON;

import java.util.Objects;

/**
 * ProtocolUtils
 *
 * @author tech@intellij.io
 */
public class ExchangeProtocolUtils {

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

}
