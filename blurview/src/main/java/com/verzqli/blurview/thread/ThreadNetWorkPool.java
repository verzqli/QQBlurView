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
class ThreadNetWorkPool extends ThreadSmartPool {
    private static final int BLOCKING_QUEUE_SIZE = (ThreadSetting.isPublicVersion ? 128 : 64);
    private static final int CORE_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_TIME = 2;
    private static final int MAX_POOL_SIZE = (Math.max(5, maximumPoolSize) * 2);

    public static ThreadSmartPool createThreadPool() {
        return new ThreadNetWorkPool(new LinkedBlockingQueue(BLOCKING_QUEUE_SIZE), new PriorityThreadFactory("thread_sp_net_", 2));
    }

    private ThreadNetWorkPool(BlockingQueue<Runnable> blockingQueue, PriorityThreadFactory threadFactory) {
        super(5, MAX_POOL_SIZE, 2, blockingQueue, threadFactory);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    protected String getName() {
        return "ThreadNetWorkPool";
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInNet;
    }
}