package com.verzqli.blurview.thread;


import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.HandlerThread;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */

public class ThreadExcutor {
    private static volatile Handler DISPATCHER_HANDLER = null;
    public static final int IS_ASYNC_POOL = 10;
    public static final int IS_DB_POOL = 7;
    public static final int IS_DOWNLOAD_POOL = 5;
    public static final int IS_FILE_POOL = 8;
    public static final int IS_HEAVY_POOL = 2;
    public static final int IS_LIGHT_POOL = 1;
    public static final int IS_NET_POOL = 9;
    public static final int IS_NOMAL_POOL = 6;
    public static final int IS_OTHER_POOL = 11;
    private static final ThreadExcutor sExcutors = new ThreadExcutor();
    public static boolean sLooperMonitorSwitch = false;
    public static int sThreshTime = 1000;
    private ThreadSmartPool mAIODownloadThreadPool;
    private ThreadSmartPool mDBPool;
    private ThreadSmartPool mFilePool;
    private ThreadSmartPool mHeavyThreadPool;
    private ThreadSmartPool mLightThreadPool;
    private ThreadSmartPool mNetPool;
    private ThreadSmartPool mNormalPool;

    public interface IThreadListener {
        void onAdded();

        void onPostRun();

        void onPreRun();
    }

    static synchronized ThreadExcutor getInstance() {
        ThreadExcutor threadExcutor;
        synchronized (ThreadExcutor.class) {
            threadExcutor = sExcutors;
        }
        return threadExcutor;
    }

    @TargetApi(9)
    private void initThreadPools() {
        if (this.mLightThreadPool == null) {
            this.mLightThreadPool = ThreadLightPool.createThreadPool();
            this.mLightThreadPool.allowCoreThreadTimeOut(true);
        }
        if (this.mHeavyThreadPool == null) {
            this.mHeavyThreadPool = ThreadHeavyPool.createThreadPool();
            this.mHeavyThreadPool.allowCoreThreadTimeOut(true);
        }
        if (this.mNormalPool == null) {
            this.mNormalPool = ThreadNormalPool.createThreadPool();
            this.mNormalPool.allowCoreThreadTimeOut(true);
        }
        if (this.mDBPool == null) {
            this.mDBPool = ThreadDBPool.createThreadPool();
            this.mDBPool.allowCoreThreadTimeOut(true);
        }
        if (this.mFilePool == null) {
            this.mFilePool = ThreadFilePool.createThreadPool();
            this.mFilePool.allowCoreThreadTimeOut(true);
        }
        if (this.mNetPool == null) {
            this.mNetPool = ThreadNetWorkPool.createThreadPool();
            this.mNetPool.allowCoreThreadTimeOut(true);
        }
        if (this.mAIODownloadThreadPool == null) {
            this.mAIODownloadThreadPool = ThreadAioDownloadPool.createThreadPool();
            this.mAIODownloadThreadPool.allowCoreThreadTimeOut(true);
        }

    }

    void shrinkMaxPoolSize(boolean shrink) {
        if (shrink) {
            this.mHeavyThreadPool.setMaximumPoolSize(Math.max(this.mHeavyThreadPool.getActiveCount(), this.mHeavyThreadPool.getCorePoolSize()));
            this.mAIODownloadThreadPool.setMaximumPoolSize(Math.max(this.mAIODownloadThreadPool.getActiveCount(), this.mAIODownloadThreadPool.getCorePoolSize()));
            return;
        }
        this.mHeavyThreadPool.setMaximumPoolSize(this.mHeavyThreadPool.getInitMaxPoolSize());
        this.mAIODownloadThreadPool.setMaximumPoolSize(this.mAIODownloadThreadPool.getInitMaxPoolSize());
    }

    private ThreadExcutor() {
        ThreadManagerV2.IsRunTimeShutDown = false;
        ThreadLog.printQLog(ThreadManagerV2.TAG, "ThreadExcutor singleton construct");
        initThreadPools();
        initDispatcherHandler();
    }

    void post(int priority, Runnable job, IThreadListener listener, boolean canAutoRetrieve) {
        if (job == null) {
            throw new IllegalArgumentException("ThreadManager job == null");
        }
        final int i = priority;
        final Runnable runnable = job;
        final IThreadListener iThreadListener = listener;
        final boolean z = canAutoRetrieve;
        DISPATCHER_HANDLER.postAtFrontOfQueue(new Runnable() {
            public void run() {
                Job w = ThreadExcutor.buildJob(i, runnable, iThreadListener, z);
                if (w == null) {
                    ThreadLog.printQLog(ThreadManagerV2.TAG, "post 3:w == null" + runnable);
                } else if (i >= 8) {
                    w.poolNum = 1;
                    ThreadExcutor.this.mLightThreadPool.execute(w);
                } else {
                    w.poolNum = 2;
                    ThreadExcutor.this.mHeavyThreadPool.execute(w);
                }
            }
        });
    }

    void excute(Runnable job, int type, IThreadListener listener, boolean canAutoRetrieve) {
        if (job == null) {
            throw new IllegalArgumentException();
        }
        final int i = type;
        final Runnable runnable = job;
        final IThreadListener iThreadListener = listener;
        final boolean z = canAutoRetrieve;
        DISPATCHER_HANDLER.postAtFrontOfQueue(new Runnable() {
            public void run() {
                Job w = ThreadExcutor.buildJob(i, runnable, iThreadListener, z);
                if (w == null) {
                    ThreadExcutor.doRdmReport("ThreadManagerV2_excute_Job_NULL", "w_NONE_job" + runnable.getClass().getName());
                    return;
                }
                ThreadSmartPool tsp;
                if ((i & 128) != 0) {
                    w.poolNum = 9;
                    tsp = ThreadExcutor.this.mNetPool;
                } else if ((i & 64) != 0) {
                    w.poolNum = 8;
                    tsp = ThreadExcutor.this.mFilePool;
                } else if ((i & 32) != 0) {
                    w.poolNum = 7;
                    tsp = ThreadExcutor.this.mDBPool;
                } else if ((i & 16) != 0) {
                    w.poolNum = 6;
                    tsp = ThreadExcutor.this.mNormalPool;
                } else {
                    return;
                }
                tsp.execute(w);
            }
        });
    }

    public static Job buildJob(int type, Runnable job, IThreadListener listener, boolean canAutoRetrieve) {
        Object obj;
        Object obj2 = null;
        Class<?> clss = job.getClass();
        String clssName = clss.getName();
        if (canAutoRetrieve) {
            try {
                Field f = clss.getDeclaredField("this$0");
                f.setAccessible(true);
                obj2 = f.get(job);
                f.set(job, null);
                obj = obj2;
            } catch (NoSuchFieldException e) {
                if (ThreadSetting.logcatBgTaskMonitor) {
                    ThreadLog.printQLog(ThreadManagerV2.TAG, "buildJob NoSuchFieldException");
                }
                obj = obj2;
            } catch (IllegalArgumentException e2) {
                if (ThreadSetting.logcatBgTaskMonitor) {
                    ThreadLog.printQLog(ThreadManagerV2.TAG, "buildJob IllegalArgumentException");
                }
                obj = obj2;
            } catch (IllegalAccessException e3) {
                if (ThreadSetting.logcatBgTaskMonitor) {
                    ThreadLog.printQLog(ThreadManagerV2.TAG, "buildJob IllegalAccessException");
                }
                obj = obj2;
            }
        } else {
            obj = null;
        }
        try {
            return new Job(obj, clssName, type, job, listener, canAutoRetrieve);
        } catch (OutOfMemoryError e4) {
            ThreadLog.printQLog(ThreadManagerV2.TAG, "buildJob IllegalAccessException", e4);
            return null;
        }
    }

    void postDownLoadTask(int priority, Runnable job, IThreadListener listener, boolean canAutoRetrieve) {
        if (job == null) {
            throw new IllegalArgumentException();
        }
        final int i = priority;
        final Runnable runnable = job;
        final IThreadListener iThreadListener = listener;
        final boolean z = canAutoRetrieve;
        DISPATCHER_HANDLER.postAtFrontOfQueue(new Runnable() {
            public void run() {
                Job w = ThreadExcutor.buildJob(i, runnable, iThreadListener, z);
                if (w == null) {
                    ThreadLog.printQLog(ThreadManagerV2.TAG, "postDownLoadTask -1:w == null" + runnable);
                    return;
                }
                w.poolNum = 5;
                ThreadExcutor.this.mAIODownloadThreadPool.execute(w);
            }
        });
    }

    void postImmediately(Runnable job, IThreadListener listener, boolean canAutoRetrieve) {
        post(10, job, listener, canAutoRetrieve);
    }

    Thread newFreeThread(Runnable job, String name, int priority) {
        Thread t = new Thread(job, name);
        t.setPriority(priority);
        return t;
    }

    HandlerThread newFreeHandlerThread(String name, int priority) {
        return new HandlerThread(name, priority) {
            public boolean quit() {
                checkQQGlobalThread();
                return super.quit();
            }

            public boolean quitSafely() {
                checkQQGlobalThread();
                return super.quitSafely();
            }

            private void checkQQGlobalThread() {
                if (!ThreadSetting.isPublicVersion) {
                    String name = getName();
                    if (name.equals("QQ_FILE_RW") || name.equals("QQ_SUB") || name.equals("Recent_Handler")) {
                        throw new RuntimeException(name + " can't quit Global Thread ");
                    }
                }
            }
        };
    }

    @TargetApi(9)
    Executor newFreeThreadPool(ThreadPoolParams threadPoolParams) {
        if (threadPoolParams == null) {
            threadPoolParams = new ThreadPoolParams();
        }
        ThreadSmartPool threadSmartPool = new ThreadSmartPool(threadPoolParams.corePoolsize, threadPoolParams.maxPooolSize, (long) threadPoolParams.keepAliveTime, threadPoolParams.queue, new PriorityThreadFactory(threadPoolParams.poolThreadName, threadPoolParams.priority));
        if (VERSION.SDK_INT > 8) {
            threadSmartPool.allowCoreThreadTimeOut(true);
        }
        ThreadLog.printQLog(ThreadManagerV2.TAG, "newFreeThreadPool " + threadPoolParams.poolThreadName);
        return threadSmartPool;
    }

    boolean removeJobFromThreadPool(Runnable job, int type) {
        if (job == null) {
            try {
                doRdmReport("removeJobFromThreadPool_Err", "job_NONE_type" + type);
                return false;
            } catch (Exception e) {
                doRdmReport("removeJobFromThreadPool_Err", "name_" + job + "_Type_" + type);
                return false;
            }
        }
        Job work = buildJob(type, job, null, false);
        if (work == null) {
            doRdmReport("removeJobFromThreadPool_Err", "work_NONE_type" + type);
            return false;
        }
        ThreadSmartPool tsp;
        if ((type & 128) != 0) {
            tsp = this.mNetPool;
        } else if ((type & 64) != 0) {
            tsp = this.mFilePool;
        } else if ((type & 32) != 0) {
            tsp = this.mDBPool;
        } else if ((type & 16) != 0) {
            tsp = this.mNormalPool;
        } else {
            doRdmReport("removeJobFromThreadPool_Err", "type_NONE_" + job);
            return false;
        }
        return tsp.remove(work);
    }

    public static void doRdmReport(String tag, String extraMsg) {
        if (!ThreadSetting.isPublicVersion) {
            throw new TSPInvalidArgsCatchedException(tag + "|" + extraMsg);
        } else if (ThreadManagerV2.sThreadWrapContext != null) {
            ThreadManagerV2.sThreadWrapContext.reportRDMException(new TSPInvalidArgsCatchedException(tag), tag, extraMsg);
        }
    }

    String printCurrentState() {
        StringBuilder sb = getAllPoolRunningJob("CRASH");
        sb.append("\n").append(this.mHeavyThreadPool.toString());
        sb.append("\n").append(this.mLightThreadPool.toString());
        sb.append("\n").append(this.mAIODownloadThreadPool.toString());
        sb.append("\n").append(this.mNormalPool.toString());
        sb.append("\n").append(this.mDBPool.toString());
        sb.append("\n").append(this.mFilePool.toString());
        sb.append("\n").append(this.mNetPool.toString());
        return sb.toString();
    }

    private StringBuilder getAllPoolRunningJob(String from) {
        ThreadLog.printQLog(ThreadManagerV2.TAG, "\ngetAllPoolRunningJob from: " + from);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nInLight");
        getPoolRunningJob(stringBuilder, Job.runningJmapInLight);
        stringBuilder.append("\nInHeavy");
        getPoolRunningJob(stringBuilder, Job.runningJmapInHeavy);
        stringBuilder.append("\nInDownload");
        getPoolRunningJob(stringBuilder, Job.runningJmapInDownload);
        stringBuilder.append("\nInNormal");
        getPoolRunningJob(stringBuilder, Job.runningJmapInNormal);
        stringBuilder.append("\nInDB");
        getPoolRunningJob(stringBuilder, Job.runningJmapInDB);
        stringBuilder.append("\nInFile");
        getPoolRunningJob(stringBuilder, Job.runningJmapInFile);
        stringBuilder.append("\nInNet");
        getPoolRunningJob(stringBuilder, Job.runningJmapInNet);
        stringBuilder.append("\nInAync");
        getPoolRunningJob(stringBuilder, Job.runningJmapInAync);
        stringBuilder.append("\nInOther");
        getPoolRunningJob(stringBuilder, Job.runningJmapInOther);
        return stringBuilder;
    }

    private StringBuilder getPoolRunningJob(StringBuilder stringBuilder, ConcurrentLinkedQueue<String> tempJ) {
        if (!(tempJ == null || stringBuilder == null)) {
            Iterator<String> iterator = tempJ.iterator();
            while (iterator.hasNext()) {
                stringBuilder.append("\nRunning_Job: " + ((String) iterator.next()));
            }
        }
        return stringBuilder;
    }

    private Handler initDispatcherHandler() {
        if (DISPATCHER_HANDLER == null) {
            HandlerThread t = newFreeHandlerThread("QQ_DISPATCHER", 0);
            t.start();
            DISPATCHER_HANDLER = new Handler(t.getLooper());
            if (ThreadSetting.logcatBgTaskMonitor) {
                DISPATCHER_HANDLER.getLooper().setMessageLogging(new ThreadLooperPrinter2(1, "QQ_DISPATCHER"));
            }
        }
        return DISPATCHER_HANDLER;
    }
}
