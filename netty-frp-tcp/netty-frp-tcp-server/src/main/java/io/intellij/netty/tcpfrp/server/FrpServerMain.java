package io.intellij.netty.tcpfrp.server;

import static io.intellij.netty.tcpfrp.SysConfig.CONFIG_PATH_PROPERTY;
import static io.intellij.netty.tcpfrp.SysConfig.DEF_SERVER_CONFIG;
import static io.intellij.netty.tcpfrp.config.ServerConfig.loadConfig;

/**
 * FrpServerMain
 *
 * @author tech@intellij.io
 */
public class FrpServerMain {

    public static void main(String[] args) {
        loadConfig(System.getProperty(CONFIG_PATH_PROPERTY, DEF_SERVER_CONFIG))
                .then(FrpServer::startServer);
    }

}
