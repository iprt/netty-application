package io.intellij.netty.utils;

/**
 * ByteUtils
 *
 * @author tech@intellij.io
 */
public class ByteUtils {

    public static byte[] getIntBytes(int number) {
        byte byte1 = (byte) (number >> 24);
        byte byte2 = (byte) (number >> 16);
        byte byte3 = (byte) (number >> 8);
        byte byte4 = (byte) (number);
        return new byte[]{byte1, byte2, byte3, byte4};
    }

}
