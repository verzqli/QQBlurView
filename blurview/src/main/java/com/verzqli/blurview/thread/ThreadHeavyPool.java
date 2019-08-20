package com.verzqli.blurview.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class ThreadHeavyPool extends ThreadSmartPool {
    private static final int BLOCKING_QUEUE_SIZE = 15;
    private static final int CORE_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_TIME = 2;
    private static final int MAX_POOL_SIZE = 64;

    public static ThreadSmartPool createThreadPool() {
        return new ThreadHeavyPool(new LinkedBlockingQueue(15), new PriorityThreadFactory("thread_heavy_", 2));
    }

    private ThreadHeavyPool(BlockingQueue<Runnable> blockingQueue, PriorityThreadFactory threadFactory) {
        super(5, 64, 2, blockingQueue, threadFactory);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    protected String getName() {
        return "ThreadHeavyPool";
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInHeavy;
    }
}