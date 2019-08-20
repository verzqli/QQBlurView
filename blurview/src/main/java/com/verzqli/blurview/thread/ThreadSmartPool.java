package com.verzqli.blurview.thread;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class ThreadSmartPool extends ThreadPoolExecutor {
    private static int BLOCKING_TIME_OUT = 9990000;
    private static int CHECK_PERIOD = 9990000;
    protected static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAX_Report_Size = 1;
    protected static final int maximumPoolSize = ((CPU_COUNT * 2) + 1);
    private Handler REJECTED_THREAD_HANDLER;
    private int blockingReportCount = 0;
    private int initMaxPoolSize;
    protected long poolcheckTime = -1;
    private boolean sAlreadyOutOfPool = false;
    private SmartRejectedExecutionHandler smartRejectedExecutionHandler = new SmartRejectedExecutionHandler();

    private class SmartRejectedExecutionHandler implements RejectedExecutionHandler {
        private int rejectReportCount = 0;

        public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
            ThreadSmartPool.this.sAlreadyOutOfPool = true;
            if (needReportRejectedError() && (executor instanceof ThreadSmartPool)) {
                String poolName = ((ThreadSmartPool) executor).getName();
                String TagName = poolName + "_RejectedExecution";
                StringBuilder sb = new StringBuilder();
                sb.append("\n revision:" + ThreadSetting.revision);
                ThreadSmartPool.this.getRunningJob(TagName, sb);
                sb.append("\n" + TagName + executor.toString());
                ThreadLog.printQLog(ThreadManagerV2.TAG, TagName + sb.toString());
                if (ThreadManagerV2.sThreadWrapContext != null) {
                    ThreadManagerV2.sThreadWrapContext.reportRDMException(new TSPRejectedCatchedException(TagName), TagName, sb.toString());
                    this.rejectReportCount++;
                    HashMap map = new HashMap();
                    map.put("executor", poolName);
                    map.put("process", String.valueOf(ThreadSetting.sProcessId));
                    ThreadManagerV2.sThreadWrapContext.reportDengTaException("", "sp_reject_exception_report", true, 0, 0, map, "", false);
                }
            }
            ThreadSmartPool.this.doJobOneByOne(task);
        }

        private boolean needReportRejectedError() {
            if (this.rejectReportCount >= 1 || !ThreadLog.needRecordJob()) {
                return false;
            }
            return true;
        }
    }

    ThreadSmartPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, BlockingQueue<Runnable> queue, PriorityThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, queue, threadFactory);
        setRejectedExecutionHandler(this.smartRejectedExecutionHandler);
        this.initMaxPoolSize = maximumPoolSize;
    }

    int getInitMaxPoolSize() {
        return this.initMaxPoolSize;
    }

    public void execute(Runnable command) {
        if (ThreadManagerV2.IsRunTimeShutDown) {
            ThreadLog.printQLog(ThreadManagerV2.TAG, "pool has shutdown:" + command.toString());
            return;
        }
        Job job;
        if (command instanceof Job) {
            job = (Job) command;
        } else {
            if (ThreadSetting.logcatBgTaskMonitor) {
                ThreadLog.printQLog(ThreadManagerV2.TAG, "command is not instanceof Job " + command.toString());
            }
            if (this instanceof ThreadAsyncTaskPool) {
                job = ThreadExcutor.buildJob(256, command, null, false);
                job.poolNum = 10;
            } else {
                job = ThreadExcutor.buildJob(512, command, null, false);
                job.poolNum = 11;
            }
            if (job == null) {
                ThreadLog.printQLog(ThreadManagerV2.TAG, "sp execute job == null ");
                doJobOneByOne(command);
                return;
            }
        }
        try {
            if (ThreadSetting.logcatBgTaskMonitor) {
                ThreadLog.printQLog(ThreadManagerV2.TAG, "tsp execute:" + job.toString());
            }
            checkBlockingState();
            super.execute(job);
        } catch (OutOfMemoryError e) {
            ThreadLog.printQLog(ThreadManagerV2.TAG, "execute job OutOfMemoryError:" + job.toString(), e);
            doJobOneByOne(job);
        } catch (InternalError e2) {
            ThreadLog.printQLog(ThreadManagerV2.TAG, "java.lang.InternalError: Thread starting during runtime shutdown", e2);
        }
    }

    private void doJobOneByOne(Runnable command) {
        this.REJECTED_THREAD_HANDLER = getRejectedHandler();
        if (this.REJECTED_THREAD_HANDLER != null) {
            this.REJECTED_THREAD_HANDLER.post(command);
        }
    }

    protected void terminated() {
        super.terminated();
    }

    private Handler getRejectedHandler() {
        if (this.REJECTED_THREAD_HANDLER == null) {
            try {
                HandlerThread thread = ThreadExcutor.getInstance().newFreeHandlerThread(getName() + "_Rejected_Handler", 10);
                thread.start();
                this.REJECTED_THREAD_HANDLER = new Handler(thread.getLooper());
                return this.REJECTED_THREAD_HANDLER;
            } catch (OutOfMemoryError e) {
                ThreadLog.printQLog(ThreadManagerV2.TAG, getName() + "_getRejectedHandler:", e);
            }
        }
        return this.REJECTED_THREAD_HANDLER;
    }

    protected String getName() {
        return "ThreadOtherPool";
    }

    private void checkBlockingState() {
        if (!this.sAlreadyOutOfPool && ThreadLog.needReportRunOrBlocking()) {
            long currentTime = SystemClock.uptimeMillis();
            if (currentTime - this.poolcheckTime > get_CHECK_PERIOD()) {
                this.poolcheckTime = currentTime;
                ThreadLog.printQLog(ThreadManagerV2.TAG, getName() + "_checkBlockingState");
                Iterator<Runnable> it = getQueue().iterator();
                long c = SystemClock.uptimeMillis();
                while (it.hasNext()) {
                    Job job = (Job) it.next();
                    job.blcokingCost = c - job.addPoint;
                    if (job.blcokingCost >= get_BLOCKING_TIME_OUT()) {
                        String TagName = getName() + "_BlockingException";
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("\n revision:" + ThreadSetting.revision);
                        getRunningJob(TagName, stringBuilder);
                        stringBuilder.append("\nblocking JOB: " + job.toString());
                        stringBuilder.append("\nblocking Executor:" + toString());
                        ThreadLog.printQLog(ThreadManagerV2.TAG, stringBuilder.toString());
                        if (ThreadManagerV2.OPEN_RDM_REPORT && ThreadManagerV2.sThreadWrapContext != null && this.blockingReportCount < 1) {
                            ThreadManagerV2.sThreadWrapContext.reportRDMException(new TSPBlockingCatchedException(TagName), TagName, stringBuilder.toString());
                            this.blockingReportCount++;
                            return;
                        }
                        return;
                    }
                }
            }
        }
    }

    private static long get_BLOCKING_TIME_OUT() {
        if (!ThreadSetting.isPublicVersion) {
            BLOCKING_TIME_OUT = 30000;
        }
        return (long) BLOCKING_TIME_OUT;
    }

    private static long get_CHECK_PERIOD() {
        if (!ThreadSetting.isPublicVersion) {
            CHECK_PERIOD = 30000;
        }
        return (long) CHECK_PERIOD;
    }

    private StringBuilder getRunningJob(String from, StringBuilder stringBuilder) {
        ThreadLog.printQLog(ThreadManagerV2.TAG, "\ngetRunningJob from: " + from);
        ConcurrentLinkedQueue<String> tempJ = getRunningJobCache();
        if (tempJ != null) {
            Iterator<String> iterator = tempJ.iterator();
            while (iterator.hasNext()) {
                stringBuilder.append("\n" + ((String) iterator.next()));
            }
        }
        return stringBuilder;
    }

    protected ConcurrentLinkedQueue<String> getRunningJobCache() {
        return Job.runningJmapInOther;
    }
}