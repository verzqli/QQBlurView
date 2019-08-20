package com.verzqli.blurview.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class ThreadAsyncTaskPool extends ThreadSmartPool {
    private static final int BLOCKING_QUEUE_SIZE = 128;
    private static final int CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4));
    private static final int KEEP_ALIVE_TIME = 2;
    private static final int MAX_POOL_SIZE = ((CPU_COUNT * 2) + 1);

    public static ThreadSmartPool createThreadPool() {
        return new ThreadAsyncTaskPool(new LinkedBlockingDeque(128), new PriorityThreadFactory("thread_sp_Async_", 5));
    }

    private ThreadAsyncTaskPool(BlockingQueue<Runnable> blockingQueue, PriorityThreadFactory threadFactory) {
        super(CORE_POOL_SIZE, MAX_POOL_SIZE, 2, blockingQueue, threadFactory);
    }

    protected String getName() {
        return "ThreadAsyncTaskPool";
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInAync;
    }
}