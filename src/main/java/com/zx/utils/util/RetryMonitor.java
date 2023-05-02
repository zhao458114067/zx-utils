package com.zx.utils.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author KuiChi
 * @date 2023/5/1 11:54
 */
@Component
@Slf4j
public class RetryMonitor implements ApplicationRunner {
    Set<Supplier<?>> retryFunctionSet = new HashSet<>();

    Map<Supplier<?>, Integer> failedMap = new HashMap<>(8);

    private final ScheduledExecutorService retryTaskExecutor = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("retryTaskExecutor").daemon(true).build());

    public void registryRetry(Supplier<?> retryFunction) {
        try {
            retryFunction.get();
        } catch (Exception e) {
            retryFunctionSet.add(retryFunction);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run(ApplicationArguments args) {
        retryTaskExecutor.scheduleAtFixedRate(() -> {
            List<Supplier<?>> needRemoveFunction = new ArrayList<>();
            for (Supplier<?> supplier : retryFunctionSet) {
                try {
                    supplier.get();
                    needRemoveFunction.add(supplier);
                } catch (Exception e) {
                    Integer fieldCount = failedMap.get(supplier);
                    fieldCount = fieldCount == null ? 1 : fieldCount + 1;
                    failedMap.put(supplier, fieldCount);
                    if (fieldCount >= 3) {
                        needRemoveFunction.add(supplier);
                        failedMap.remove(supplier);
                    }
                    log.error("重试第 {} 次发生异常，errorMessage：{}", fieldCount, e.getMessage());
                    e.printStackTrace();
                }
            }
            for (Supplier<?> retryFunction : needRemoveFunction) {
                retryFunctionSet.remove(retryFunction);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }
}
