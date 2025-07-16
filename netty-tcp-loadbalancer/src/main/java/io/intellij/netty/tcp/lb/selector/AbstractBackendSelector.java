package io.intellij.netty.tcp.lb.selector;

import io.intellij.netty.tcp.lb.config.Backend;
import org.jetbrains.annotations.NotNull;

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
public abstract class AbstractBackendSelector implements BackendSelector {
    // available status map
    private final Map<String, Boolean> AVAILABLE_STATUS = new ConcurrentHashMap<>();
    // connection count map
    private final Map<String, Integer> CONNECTION_COUNT = new ConcurrentHashMap<>();

    protected final Map<String, Backend> backends;

    protected AbstractBackendSelector(Map<String, Backend> backends) {
        this.backends = backends;
    }

    @Override
    public Backend select() {
        return this.doSelect();
    }

    @Override
    public Backend nextIfConnectFailed(Backend failed) {
        AVAILABLE_STATUS.put(failed.getName(), false);
        return this.doSelect();
    }

    protected abstract Backend doSelect();

    @Override
    public void onChannelActive(Backend target) {
        String name = target.getName();
        AVAILABLE_STATUS.put(name, true);
        CONNECTION_COUNT.put(name, CONNECTION_COUNT.getOrDefault(name, 0) + 1);
        this.afterActive(target);
    }

    @Override
    public void onChannelInactive(@NotNull Backend target) {
        String key = target.getName();
        CONNECTION_COUNT.put(key, CONNECTION_COUNT.get(key) - 1);
        this.afterInactive(target);
    }

    protected void afterActive(Backend target) {
        // hook method for subclasses
    }

    protected void afterInactive(Backend target) {
        // hook method for subclasses
    }

    @Override
    public void reset() {
        AVAILABLE_STATUS.clear();
        CONNECTION_COUNT.clear();
    }

    protected Map<String, Boolean> accessStatusMap() {
        return AVAILABLE_STATUS;
    }

    protected Map<String, Integer> connectionCountMap() {
        return CONNECTION_COUNT;
    }

    protected List<String> availableList() {
        return this.backends.keySet().stream().sorted()
                .filter(name -> Objects.isNull(accessStatusMap().get(name)) || accessStatusMap().get(name))
                .toList();
    }

}
