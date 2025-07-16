package io.intellij.netty.tcp.lb.selector.strategies;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.selector.AbstractBackendSelector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RoundRobinSelector
 *
 * @author tech@intellij.io
 */
public class RoundRobinSelector extends AbstractBackendSelector {
    private static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(-1);

    private final Map<String, Integer> backendIndex;
    private final Map<Integer, Backend> indexBackend;

    public RoundRobinSelector(Map<String, Backend> backends) {
        super(backends);
        backendIndex = new HashMap<>();
        indexBackend = new HashMap<>();
        List<String> names = this.backends.keySet().stream().sorted().toList();

        for (int i = 0; i < names.size(); i++) {
            backendIndex.put(names.get(i), i);
            indexBackend.put(i, backends.get(names.get(i)));
        }
    }

    @Override
    protected void afterActive(Backend target) {
        String name = target.getName();
        ROUND_ROBIN_INDEX.set(backendIndex.get(name));
    }

    @Override
    protected Backend doSelect() {
        List<String> availableList = availableList();
        if (availableList.isEmpty()) {
            return null;
        }
        int i = ROUND_ROBIN_INDEX.get();
        int size = availableList.size();
        if (i < 0 || size == 1) {
            return indexBackend.get(0);
        }

        int index = (i + 1) % size;
        return indexBackend.get(index);
    }


}
