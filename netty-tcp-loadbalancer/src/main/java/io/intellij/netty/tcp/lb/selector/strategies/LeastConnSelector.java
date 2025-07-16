package io.intellij.netty.tcp.lb.selector.strategies;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.selector.AbstractBackendSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * LeastConnSelector
 *
 * @author tech@intellij.io
 */
@Slf4j
public class LeastConnSelector extends AbstractBackendSelector {

    public LeastConnSelector(Map<String, Backend> backends) {
        super(backends);
    }

    @Override
    protected void afterActive(Backend target) {
        connectionCountMap().forEach((k, v) -> log.info("name = {}, count = {}", k, v));
    }

    @Override
    protected void afterInactive(Backend target) {
        connectionCountMap().forEach((k, v) -> log.info("name = {}, count = {}", k, v));
    }

    @Override
    protected Backend doSelect() {
        // 可以用堆优化 选择连接数最少的后端
        List<String> availableList = this.availableList();
        if (availableList.isEmpty()) {
            return null;
        }

        if (availableList.size() == 1) {
            return backends.get(availableList.get(0));
        }

        String minKey = availableList.get(0);
        Integer min = this.connectionCountMap().get(minKey);
        if (Objects.isNull(min)) {
            return backends.get(minKey);
        }

        for (int i = 1; i < availableList.size(); i++) {
            String key = availableList.get(i);
            Integer count = this.connectionCountMap().get(key);
            if (Objects.isNull(count)) {
                return backends.get(key);
            }

            if (count < min) {
                min = count;
                minKey = key;
            }
        }
        return backends.get(minKey);
    }

}
