package io.intellij.netty.tcpfrp.server.listening;

import io.intellij.netty.tcpfrp.protocol.server.ListeningResponse;
import io.intellij.netty.utils.ServerSocketUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MultiPortUtils
 *
 * @author tech@intellij.io
 */
public class MultiPortUtils {

    public static ListeningResponse test(@NotNull List<Integer> listeningPorts) {
        Map<Integer, Boolean> listeningStatus = new HashMap<>();
        for (int port : listeningPorts) {
            boolean portInUse = ServerSocketUtils.isPortInUse(port);
            listeningStatus.put(port, portInUse);
        }
        return ListeningResponse.builder()
                .success(listeningStatus.values().stream().noneMatch(b -> b))
                .listeningStatus(listeningStatus).build();
    }

}
