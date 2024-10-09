package io.intellij.netty.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

/**
 * NetworkUtils
 *
 * @author tech@intellij.io
 */
public class ServerSocketUtils {

    public static boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port));
            return false; // 端口未被占用
        } catch (IOException e) {
            return true; // 端口被占用
        }
    }

}
