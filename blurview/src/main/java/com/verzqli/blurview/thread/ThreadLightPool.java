package com.verzqli.blurview.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class ThreadLightPool extends ThreadSmartPool {
    private static final int CORE_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 2;
    private static final int MAX_POOL_SIZE = 128;

    public static ThreadSmartPool createThreadPool() {
        return new ThreadLightPool(new SynchronousQueue(true), new PriorityThreadFactory("thread_light_", 2));
    }

    private ThreadLightPool(BlockingQueue<Runnable> blockingQueue, PriorityThreadFactory threadFactory) {
        super(2, 128, 2, blockingQueue, threadFactory);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    protected String getName() {
        return "ThreadLightPool";
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInLight;
    }
}