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
class ThreadNormalPool extends ThreadSmartPool {
    private static final int BLOCKING_QUEUE_SIZE = (ThreadSetting.isPublicVersion ? 128 : 64);
    private static final int CORE_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_TIME = 2;
    private static final int MAX_POOL_SIZE = Math.max(5, maximumPoolSize);

    public static ThreadSmartPool createThreadPool() {
        return new ThreadNormalPool(new LinkedBlockingDeque(BLOCKING_QUEUE_SIZE), new PriorityThreadFactory("thread_sp_normal_", 5));
    }

    private ThreadNormalPool(BlockingQueue<Runnable> blockingQueue, PriorityThreadFactory threadFactory) {
        super(5, MAX_POOL_SIZE, 2, blockingQueue, threadFactory);
    }

    protected String getName() {
        return "ThreadNormalPool";
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInNormal;
    }
}