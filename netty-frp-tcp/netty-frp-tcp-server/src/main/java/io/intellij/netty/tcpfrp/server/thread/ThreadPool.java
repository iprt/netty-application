package io.intellij.netty.tcpfrp.server.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ThreadPool
 *
 * @author tech@intellij.io
 */
public class ThreadPool {
    public static ExecutorService ES = Executors.newCachedThreadPool();
}
