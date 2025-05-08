package io.intellij.netty.spring.boot.netty;

import io.intellij.netty.spring.boot.entities.NettyServerConf;
import io.intellij.netty.spring.boot.entities.NettySeverRunRes;

/**
 * NettyServerGroup
 *
 * @author tech@intellij.io
 */
public interface NettyServerGroup {

    /**
     * Starts a Netty server with the specified configuration.
     *
     * @param conf the configuration for the Netty server, including the port to bind
     * @return the result of starting the server, containing the success status and any message
     */
    NettySeverRunRes start(NettyServerConf conf);

    /**
     * Checks whether the server is currently running on the given port.
     *
     * @param port the port number to check if the server is running on
     * @return true if the server is running on the specified port, false otherwise
     */
    boolean isRunning(int port);

    /**
     * Stops the Netty server running on the specified port.
     *
     * @param port the port number on which the server is running and needs to be stopped
     */
    void stop(int port);

}
