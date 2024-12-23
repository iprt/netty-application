package io.intellij.netty.spring.boot.controller;

import io.intellij.netty.spring.boot.entities.NettyServerConf;
import io.intellij.netty.spring.boot.entities.NettySeverRunRes;
import io.intellij.netty.spring.boot.netty.NettyServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * NettyServerController
 *
 * @author tech@intellij.io
 */
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/netty")
@RestController
@Slf4j
public class NettyServerController {
    private final NettyServer nettyServer;

    @PostMapping("/server/start")
    public NettySeverRunRes startServer(@RequestBody @Validated NettyServerConf conf) {
        log.info("Starting Netty Server");
        return nettyServer.start(conf);
    }

    @PostMapping("/server/status")
    public Map<String, Object> isServerRunning(@RequestBody @Validated NettyServerConf conf) {
        log.info("Checking if Netty Server is running");
        if (nettyServer.isRunning(conf.getPort())) {
            log.info("Netty Server is running");
            return Map.of(
                    "code", 200,
                    "port", conf.getPort(),
                    "msg", "Netty Server is running");
        } else {
            log.info("Netty Server is not running");
            return Map.of(
                    "code", 500,
                    "port", conf.getPort(),
                    "msg", "Netty Server is not running");
        }
    }

    @PostMapping("/server/stop")
    public void stopServer(@RequestBody @Validated NettyServerConf conf) {
        log.warn("Stopping Netty Server");
        nettyServer.stop(conf.getPort());
    }

}
