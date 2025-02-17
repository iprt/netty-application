package io.intellij.netty.tcpfrp.server;

import io.intellij.netty.tcpfrp.config.ServerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ServerConfigTest
 *
 * @author tech@intellij.io
 */
public class ServerConfigTest {

    @Test
    public void testGetServerConfig() {
        ServerConfig config = ServerConfig.init(ServerConfigTest.class.getClassLoader().getResourceAsStream("server-config.json"));
        System.err.println(config);
        Assertions.assertTrue(config.isValid());
    }

}
