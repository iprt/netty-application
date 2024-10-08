package io.intellij.netty.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * NetworkUtils
 *
 * @author tech@intellij.io
 */
public class SocketUtils {

    public static boolean isPortOpen(String host, int port) {
        return isPortOpen(host, port, 3);
    }

    public static boolean isPortOpen(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeout);
            return true; // 端口打开
        } catch (IOException e) {
            return false; // 端口关闭或不可达
        }
    }

}
