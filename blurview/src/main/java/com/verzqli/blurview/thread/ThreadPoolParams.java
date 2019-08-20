package com.verzqli.blurview.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */

public class ThreadPoolParams {
    public static final String DEFAULT_THREAD_NAME = "default_name";
    public int corePoolsize = 3;
    public int keepAliveTime = 1;
    public int maxPooolSize = 5;
    public String poolThreadName = DEFAULT_THREAD_NAME;
    public int priority = 5;
    public BlockingQueue<Runnable> queue = new LinkedBlockingQueue(128);
}