package com.verzqli.blurview.thread;

import java.util.concurrent.ThreadFactory;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class PriorityThreadFactory implements ThreadFactory {
    public String mName;
    public int mPriority;
    public volatile int threadIndex = 0;

    PriorityThreadFactory(String mName, int mPriority) {
        this.mPriority = mPriority;
        this.mName = mName;
    }

    public Thread newThread(Runnable r) {
        this.threadIndex++;
        if (this.threadIndex > 10000) {
            this.threadIndex = 0;
        }
        return new Thread(r, this.mName + this.threadIndex);
    }
}