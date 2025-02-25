package io.intellij.netty.tcp.lb.strategy;

import io.intellij.netty.tcp.lb.config.Backend;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AbstractBackendChooser
 *
 * @author tech@intellij.io
 * @since 2025-02-24
 */
public abstract class AbstractBackendChooser implements BackendChooser {
    private static final Map<String, Boolean> ACCESS_STATUS = new ConcurrentHashMap<>();
    private static final Map<String, Integer> CONNECTION_COUNT = new ConcurrentHashMap<>();

    protected final Map<String, Backend> backends;

    protected AbstractBackendChooser(Map<String, Backend> backends) {
        this.backends = backends;
    }

    @Override
    public Backend receive() {
        return this.choose();
    }

    @Override
    public Backend next(Backend choose) {
        String key = choose.getName();
        ACCESS_STATUS.put(key, false);
        return this.choose();
    }

    @Override
    public void active(Backend target) {
        String key = target.getName();
        ACCESS_STATUS.put(key, true);
        CONNECTION_COUNT.put(key, CONNECTION_COUNT.getOrDefault(key, 0) + 1);

        this.afterActive(target);
    }

    protected void afterActive(Backend target) {
        // do nothing
    }

    @Override
    public void inactive(Backend target) {
        String key = target.getName();
        CONNECTION_COUNT.put(key, CONNECTION_COUNT.get(key) - 1);

        this.afterInactive(target);
    }

    protected void afterInactive(Backend target) {
        // do nothing
    }

    protected Map<String, Boolean> accessStatusMap() {
        return ACCESS_STATUS;
    }

    protected Map<String, Integer> connectionCountMap() {
        return CONNECTION_COUNT;
    }

    protected List<String> availableList() {
        return this.backends.keySet().stream().sorted()
                .filter(name -> Objects.isNull(accessStatusMap().get(name)) || accessStatusMap().get(name))
                .toList();
    }

    protected void logAccessStatus(Logger log) {
        ACCESS_STATUS.forEach((k, v) -> log.info("name = {}, status = {}", k, v));
    }

    protected void logConnectionCount(Logger log) {
        CONNECTION_COUNT.forEach((k, v) -> log.info("name = {}, count = {}", k, v));
    }

    protected abstract Backend choose();

}
