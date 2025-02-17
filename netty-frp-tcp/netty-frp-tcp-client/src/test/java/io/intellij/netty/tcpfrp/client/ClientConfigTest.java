package io.intellij.netty.tcpfrp.client;

import io.intellij.netty.tcpfrp.config.ClientConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ClientConfigTest
 *
 * @author tech@intellij.io
 */
public class ClientConfigTest {

    @Test
    public void testGetClientConfig() {
        ClientConfig clientConfig = ClientConfig.init(ClientConfigTest.class.getClassLoader().getResourceAsStream("client-config.json"));
        System.err.println(clientConfig);
        Assertions.assertTrue(clientConfig.isValid());
    }

}
