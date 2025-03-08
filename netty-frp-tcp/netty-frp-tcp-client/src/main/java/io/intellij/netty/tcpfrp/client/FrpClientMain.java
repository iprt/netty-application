package io.intellij.netty.tcpfrp.client;

import static io.intellij.netty.tcpfrp.SysConfig.CONFIG_PATH_PROPERTY;
import static io.intellij.netty.tcpfrp.SysConfig.DEF_CLIENT_CONFIG;
import static io.intellij.netty.tcpfrp.config.ClientConfig.loadConfig;

/**
 * FrpClientMain
 *
 * @author tech@intellij.io
 */
public class FrpClientMain {

    public static void main(String[] args) {
        loadConfig(System.getProperty(CONFIG_PATH_PROPERTY, DEF_CLIENT_CONFIG))
                .then(FrpClient::start);
    }

}
