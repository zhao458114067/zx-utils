package com.zx.utils.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author ZhaoXu
 * @date 2023/5/1 11:54
 */
public class RetryMonitor {
    @FunctionalInterface
    public interface Execute {
        void execute();
    }

    private static final Logger log = LoggerFactory.getLogger(RetryMonitor.class);

    private static final BlockingQueue<Pair<Execute, Integer>> failedQueue = new LinkedBlockingQueue<>();

    private static final ScheduledExecutorService retryTaskExecutor = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("retryTaskExecutor").
                    daemon(true)
                    .build());

    static {
        retryTaskExecutor.scheduleAtFixedRate(() -> {
            Pair<Execute, Integer> take = null;
            try {
                take = failedQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Execute key = take.getKey();
            try {
                key.execute();
            } catch (Exception e) {
                Integer failedNumber = take.getValue();
                log.error("重试第 {} 次失败", ++failedNumber, e);
                if (failedNumber < 3) {
                    failedQueue.offer(Pair.of(key, failedNumber));
                }
            }
        }, 5, 15, TimeUnit.SECONDS);
    }

    public static void registry(Execute retryFunction) {
        if (Objects.isNull(retryFunction)) {
            return;
        }
        try {
            retryFunction.execute();
        } catch (Exception e) {
            failedQueue.offer(Pair.of(retryFunction, 1));
            log.error("执行 1/3 失败，进入重试队列", e);
        }
    }
}
