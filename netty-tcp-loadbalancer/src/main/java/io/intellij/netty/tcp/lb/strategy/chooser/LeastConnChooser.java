package io.intellij.netty.tcp.lb.strategy.chooser;

import io.intellij.netty.tcp.lb.config.Backend;
import io.intellij.netty.tcp.lb.strategy.AbstractBackendChooser;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * LeastConnChooser
 *
 * @author tech@intellij.io
 * @since 2025-02-25
 */
@Slf4j
public class LeastConnChooser extends AbstractBackendChooser {

    public LeastConnChooser(Map<String, Backend> backends) {
        super(backends);
    }

    @Override
    protected void afterActive(Backend target) {
        this.logConnectionCount(log);
    }

    @Override
    protected void afterInactive(Backend target) {
        this.logConnectionCount(log);
    }

    @Override
    protected Backend choose() {
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
