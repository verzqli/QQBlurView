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
class ThreadAioDownloadPool extends ThreadSmartPool {
    private static final int BLOCKING_QUEUE_SIZE = 64;
    private static final int CORE_POOL_SIZE = 3;
    private static final int KEEP_ALIVE_TIME = 2;
    private static final int MAX_POOL_SIZE = Math.max(3, maximumPoolSize);

    public static ThreadSmartPool createThreadPool() {
        return new ThreadAioDownloadPool(new LinkedBlockingQueue(64), new PriorityThreadFactory("thread_AioDownload_", 2));
    }

    private ThreadAioDownloadPool(BlockingQueue<Runnable> blockingQueue, PriorityThreadFactory threadFactory) {
        super(3, MAX_POOL_SIZE, 2, blockingQueue, threadFactory);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    protected String getName() {
        return "ThreadAioDownloadPool";
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInDownload;
    }
}