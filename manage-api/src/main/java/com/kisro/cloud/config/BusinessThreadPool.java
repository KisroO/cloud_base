package com.kisro.cloud.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Kisro
 * @since 2023/1/4
 **/
public class BusinessThreadPool {
    //    public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(4, 8, 90, TimeUnit.SECONDS,new ArrayBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());
    public static final ScheduledExecutorService THREAD_POOL_EXECUTOR = Executors.newScheduledThreadPool(3);
}
