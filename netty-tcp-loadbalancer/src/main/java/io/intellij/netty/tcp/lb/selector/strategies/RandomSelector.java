package io.intellij.netty.tcp.lb.selector.strategies;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.selector.AbstractBackendSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * RandomSelector
 *
 * @author tech@intellij.io
 */
@Slf4j
public class RandomSelector extends AbstractBackendSelector {

    public RandomSelector(Map<String, Backend> backends) {
        super(backends);
    }

    @Override
    protected void afterActive(Backend target) {
        log.info("========> active statistic start ========== | target: {}", target.getName());
        connectionCountMap().forEach((k, v) -> log.info("Random Selector|name = {}, count = {}", k, v));
        log.info("========> active statistic end   ==========\n");
    }

    @Override
    protected void afterInactive(Backend target) {
        log.info("========> inactive start ========== | target: {}", target.getName());
        connectionCountMap().forEach((k, v) -> log.info("Random Selector|name = {}, count = {}", k, v));
        log.info("========> inactive end   ==========\n");
    }

    @Override
    protected Backend doSelect() {
        Map<String, Boolean> accessMap = accessStatusMap();
        List<String> failed = accessMap.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .toList();
        if (failed.size() == backends.size()) {
            return null;
        }
        List<Backend> accessBackends = backends.values().stream()
                .filter(backend -> !failed.contains(backend.getName()))
                .toList();
        return random(accessBackends);
    }

    private Backend random(List<Backend> accessBackends) {
        int size = accessBackends.size();
        int index = (int) (Math.random() * size);
        return accessBackends.get(index);
    }

}
