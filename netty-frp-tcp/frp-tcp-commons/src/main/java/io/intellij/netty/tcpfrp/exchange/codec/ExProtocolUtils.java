package io.intellij.netty.tcpfrp.exchange.codec;

import com.alibaba.fastjson2.JSON;
import io.intellij.netty.tcpfrp.exchange.both.DataPacket;
import io.intellij.netty.tcpfrp.exchange.c2s.ServiceDataPacket;
import io.intellij.netty.tcpfrp.exchange.s2c.UserDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ProtocolUtils
 *
 * @author tech@intellij.io
 */
public class ExProtocolUtils {
    public static final Map<String, Integer> CLASSNAME_TO_TYPE;

    // private static final int TYPE_COUNT = ExchangeType.values().length;

    private static final int FIXED_CHANNEL_ID_LEN = 60;

    static final boolean DATA_PACKET_USE_JSON = Boolean.parseBoolean(System.getProperty("jsonPacket", "false"));

    public static ExchangeProtocol createProtocolData(ExchangeType exchangeType, Object obj) {
        if (Objects.isNull(exchangeType) || Objects.isNull(obj)) {
            return null;
        }
        // accelerate ?
        if (ExchangeType.S2C_USER_DATA_PACKET == exchangeType || ExchangeType.C2S_SERVICE_DATA_PACKET == exchangeType) {
            if (!DATA_PACKET_USE_JSON) {
                DataPacket dataPacket = (DataPacket) obj;
                return createDataPacket(exchangeType, dataPacket.getUserChannelId(), dataPacket.getServiceChannelId(), dataPacket.getPacket());
            }
        }

        return ExchangeProtocol.builder()
                .exchangeType(exchangeType)
                .body(JSON.toJSONString(obj).getBytes())
                .build();

    }

    private static ExchangeProtocol createDataPacket(ExchangeType exchangeType, String userChannelId, String serviceChannelId, byte[] packet) {
        byte[] userChannelIdBytes = userChannelId.getBytes();
        byte[] serviceChannelIdBytes = serviceChannelId.getBytes();

        byte[] bodyBytes = new byte[userChannelIdBytes.length + serviceChannelIdBytes.length + packet.length];

        System.arraycopy(userChannelIdBytes, 0, bodyBytes, 0, userChannelIdBytes.length);
        System.arraycopy(serviceChannelIdBytes, 0, bodyBytes, userChannelIdBytes.length, serviceChannelIdBytes.length);
        System.arraycopy(packet, 0, bodyBytes, userChannelIdBytes.length + serviceChannelIdBytes.length, packet.length);

        return ExchangeProtocol.builder()
                .exchangeType(exchangeType).className(exchangeType.getClazz().getName())
                .body(bodyBytes)
                .build();
    }

    public static <T> ProtocolParse<T> parseProtocol(@NotNull ExchangeProtocol msg, @NotNull Class<T> target) {
        ExchangeType exchangeType = msg.getExchangeType();
        String protocolClassName = exchangeType.getClazz().getName();
        String targetClassName = target.getName();
        if (protocolClassName.equals(targetClassName)) {
            try {
                T obj = JSON.parseObject(msg.getBody(), target);
                if (Objects.isNull(obj)) {
                    return ProtocolParse.<T>builder()
                            .valid(false).invalidMsg("JSON.parseObject(json, target) return null")
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
                    .valid(false).exchangeType(exchangeType)
                    .invalidMsg(
                            String.format("msg's classname does not match target's classname|protocol.classname=%s|target.classname=%s",
                                    protocolClassName, targetClassName)
                    ).build();
        }
    }

    public static ProtocolParse<DataPacket> parseDataPacket(@NotNull ExchangeProtocol msg) {
        if (DATA_PACKET_USE_JSON) {
            return parseDataPacketJson(msg);
        }
        ExchangeType exchangeType = msg.getExchangeType();
        String protocolClassName = msg.getClassName();
        if (ServiceDataPacket.class.getName().equals(protocolClassName) || UserDataPacket.class.getName().equals(protocolClassName)) {
            Class<?> targetClass = ServiceDataPacket.class.getName().equals(protocolClassName) ? ServiceDataPacket.class : UserDataPacket.class;

            byte[] bodyBytes = msg.getBody();

            byte[] userChannelIdBytes = new byte[FIXED_CHANNEL_ID_LEN];
            byte[] serviceChannelIdBytes = new byte[FIXED_CHANNEL_ID_LEN];
            byte[] packet = new byte[bodyBytes.length - 2 * FIXED_CHANNEL_ID_LEN];
            System.arraycopy(bodyBytes, 0, userChannelIdBytes, 0, FIXED_CHANNEL_ID_LEN);
            System.arraycopy(bodyBytes, FIXED_CHANNEL_ID_LEN, serviceChannelIdBytes, 0, FIXED_CHANNEL_ID_LEN);
            System.arraycopy(bodyBytes, 2 * FIXED_CHANNEL_ID_LEN, packet, 0, packet.length);

            return ProtocolParse.<DataPacket>builder()
                    .valid(true).exchangeType(exchangeType)
                    .data(DataPacket.builder()
                            .from(targetClass.getName())
                            .userChannelId(new String(userChannelIdBytes)).serviceChannelId(new String(serviceChannelIdBytes))
                            .packet(packet)
                            .build())
                    .build();
        }

        return ProtocolParse.<DataPacket>builder()
                .valid(false).exchangeType(exchangeType)
                .invalidMsg("unknown data packet")
                .build();
    }

    private static ProtocolParse<DataPacket> parseDataPacketJson(@NotNull ExchangeProtocol msg) {
        ExchangeType exchangeType = msg.getExchangeType();
        String protocolClassName = msg.getClassName();
        if (ServiceDataPacket.class.getName().equals(protocolClassName) || UserDataPacket.class.getName().equals(protocolClassName)) {
            try {
                DataPacket obj = JSON.parseObject(msg.getBody(), DataPacket.class);
                if (Objects.isNull(obj)) {
                    return ProtocolParse.<DataPacket>builder()
                            .valid(false).invalidMsg("JSON.parseObject(json, target) return null")
                            .build();
                }

                return ProtocolParse.<DataPacket>builder()
                        .valid(true).exchangeType(msg.getExchangeType())
                        .data(obj).build();

            } catch (Exception e) {
                return ProtocolParse.<DataPacket>builder()
                        .valid(false).invalidMsg(e.getMessage())
                        .build();
            }

        } else {
            return ProtocolParse.<DataPacket>builder()
                    .valid(false).exchangeType(exchangeType)
                    .invalidMsg("unknown packet type class").build();
        }
    }

    static {
        CLASSNAME_TO_TYPE = new HashMap<>();
        Arrays.stream(ExchangeType.values()).forEach(t -> CLASSNAME_TO_TYPE.put(t.getClazz().getName(), t.getType()));
    }

}
