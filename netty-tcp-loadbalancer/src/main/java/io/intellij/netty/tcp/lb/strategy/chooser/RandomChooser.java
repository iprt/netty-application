package io.intellij.netty.tcp.lb.strategy.chooser;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.strategy.AbstractBackendChooser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * RandomChooser
 *
 * @author tech@intellij.io
 * @since 2025-02-20
 */
@Slf4j
public class RandomChooser extends AbstractBackendChooser {

    public RandomChooser(Map<String, Backend> backends) {
        super(backends);
    }

    @Override
    protected void afterActive( Backend target) {
        log.info("========> active start ========== | target: {}", target.getName());
        this.logConnectionCount(log);
        log.info("========> active end   ==========");
    }

    @Override
    protected void afterInactive( Backend target) {
        log.info("========> inactive start ========== | target: {}", target.getName());
        this.logConnectionCount(log);
        log.info("========> inactive end   ==========");
    }

    @Override
    protected Backend choose() {
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
