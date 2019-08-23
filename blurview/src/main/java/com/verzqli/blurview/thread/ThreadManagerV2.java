package com.verzqli.blurview.thread;


import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;


import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManagerV2 {
    public static final String AUTO_MONITOR_TAG = "AutoMonitor";
    protected static HandlerThread FILE_THREAD = null;
    protected static volatile Handler FILE_THREAD_HANDLER = null;
    public static volatile boolean IsRunTimeShutDown = false;
    public static final boolean OLD_BUSINESS_AUTO_RETRIEVE = false;
    public static boolean OPEN_RDM_REPORT = true;
    protected static HandlerThread RECENT_THREAD = null;
    protected static Handler RECENT_THREAD_HANDLER = null;
    private static final String REPORT_AP_REJECTION_EXCEPTION = "ap_reject_exception_report";
    protected static final String REPORT_SP_REJECTION_EXCEPTION = "sp_reject_exception_report";
    protected static HandlerThread SUB_THREAD = null;
    protected static volatile Handler SUB_THREAD_HANDLER = null;
    public static final String TAG = "ThreadManager";
    private static Timer TIMER;
    protected static volatile Handler UI_HANDLER;
    private static Executor mNetExcutorPool = new ThreadPoolExecutor(5, 9, 1, TimeUnit.SECONDS, new LinkedBlockingQueue(256), new ThreadFactory() {
        public Thread newThread(Runnable runnable) {
            Log.i(ThreadManagerV2.TAG, "new NetExcutor5Thread");
            return new Thread(runnable, "NetExcutor5Thread");
        }
    });
    public static ThreadWrapContext sThreadWrapContext;

    static {
        initRuntimShutDownHook();
        reflectAsyncTaskPool();
    }

    public static void excute(Runnable job, int type, ThreadExcutor.IThreadListener listener, boolean canAutoRetrieve) {
        if ((type & 240) == 0) {
            if (!ThreadSetting.isPublicVersion) {
                ThreadLog.trackException(TAG, "ThreadManager.excute type is not valid");
            } else if (sThreadWrapContext != null) {
                String TagName = "ThreadManager_excute_Type_NONE";
                sThreadWrapContext.reportRDMException(new TSPInvalidArgsCatchedException(TagName), TagName, job.getClass().getName());
                return;
            } else {
                return;
            }
        }
        ThreadExcutor.getInstance().excute(job, type, listener, canAutoRetrieve);
    }

    private static void initRuntimShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                ThreadManagerV2.IsRunTimeShutDown = true;
                Log.i(ThreadManagerV2.TAG, "QQ Runtime ShutDown");
            }
        });
    }

    @TargetApi(11)
    private static void reflectAsyncTaskPool() {
        try {
            ThreadSmartPool tsp = ThreadAsyncTaskPool.createThreadPool();
            tsp.allowCoreThreadTimeOut(true);
            Log.i(TAG, "reflectAsyncTaskPool before:" + AsyncTask.THREAD_POOL_EXECUTOR);
            Field field = AsyncTask.class.getDeclaredField("THREAD_POOL_EXECUTOR");
            field.setAccessible(true);
            field.set(null, tsp);
            Log.i(TAG, "reflectAsyncTaskPool after:" + AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Throwable e) {
            Log.i(TAG, "reflectAsyncTaskPool", e);
        }
    }

    public static void init() {
        Log.i(TAG, "ThreadManager init");
        ThreadExcutor.getInstance();
    }

    @Deprecated
    public static void post(Runnable job, int priority, ThreadExcutor.IThreadListener listener, boolean canAutoRetrieve) {
        ThreadExcutor.getInstance().post(priority, job, listener, canAutoRetrieve);
    }

    @Deprecated
    public static void postDownLoadTask(Runnable job, int priority, ThreadExcutor.IThreadListener listener, boolean canAutoRetrieve) {
        ThreadExcutor.getInstance().postDownLoadTask(priority, job, listener, canAutoRetrieve);
    }

    @Deprecated
    public static void postImmediately(Runnable job, ThreadExcutor.IThreadListener listener, boolean canAutoRetrieve) {
        ThreadExcutor.getInstance().postImmediately(job, listener, canAutoRetrieve);
    }

    public static Thread newFreeThread(Runnable job, String name, int priority) {
        Thread t = ThreadExcutor.getInstance().newFreeThread(job, name, priority);
        Log.i(TAG, t.getId() + "|" + name);
        return t;
    }

    public static HandlerThread newFreeHandlerThread(String name, int priority) {
        HandlerThread t = ThreadExcutor.getInstance().newFreeHandlerThread(name, priority);
        Log.i(TAG, t.getId() + "-" + name);
        return t;
    }

    public static Executor newFreeThreadPool(ThreadPoolParams threadPoolParams) {
        if (ThreadSetting.isPublicVersion || (threadPoolParams != null && !TextUtils.isEmpty(threadPoolParams.poolThreadName) && !ThreadPoolParams.DEFAULT_THREAD_NAME.equals(threadPoolParams.poolThreadName))) {
            return ThreadExcutor.getInstance().newFreeThreadPool(threadPoolParams);
        }
        throw new RuntimeException("newFreeThreadPool exception");
    }

    @Deprecated
    public static boolean remove(Runnable job) {
        Log.i(TAG, "Remove_Use_Deprecated_Method " + job.getClass().getName());
        return false;
    }

    public static boolean removeJobFromThreadPool(Runnable job, int type) {
        return ThreadExcutor.getInstance().removeJobFromThreadPool(job, type);
    }

    public static String reportCurrentState() {
        return ThreadExcutor.getInstance().printCurrentState();
    }

    public static void executeOnSubThread(Runnable run) {
        getSubThreadHandlerV2().post(run);
    }

    public static void executeOnFileThread(Runnable run) {
        getFileThreadHandlerV2().post(run);
    }

    public static void executeOnNetWorkThread(Runnable run) {
        excute(run, 128, null, false);
    }

    public static Thread getFileThread() {
        if (FILE_THREAD == null) {
            getFileThreadHandlerV2();
        }
        return FILE_THREAD;
    }

    private static Handler getFileThreadHandlerV2() {
        if (FILE_THREAD_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                if (FILE_THREAD_HANDLER == null) {
                    FILE_THREAD = newFreeHandlerThread("QQ_FILE_RW", 0);
                    FILE_THREAD.start();
                    FILE_THREAD_HANDLER = new Handler(FILE_THREAD.getLooper());
                }
            }
        }
        return FILE_THREAD_HANDLER;
    }

    public static Looper getFileThreadLooper() {
        return getFileThreadHandlerV2().getLooper();
    }

    public static Thread getSubThread() {
        if (SUB_THREAD == null) {
            getSubThreadHandlerV2();
        }
        return SUB_THREAD;
    }

    private static Handler getSubThreadHandlerV2() {
        if (SUB_THREAD_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                if (SUB_THREAD_HANDLER == null) {
                    SUB_THREAD = newFreeHandlerThread("QQ_SUB", 0);
                    SUB_THREAD.start();
                    SUB_THREAD_HANDLER = new Handler(SUB_THREAD.getLooper());
                }
            }
        }
        return SUB_THREAD_HANDLER;
    }

    public static Looper getSubThreadLooper() {
        return getSubThreadHandlerV2().getLooper();
    }

    public static Handler getUIHandlerV2() {
        if (UI_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                if (UI_HANDLER == null) {
                    UI_HANDLER = new Handler(Looper.getMainLooper());
                }
            }
        }
        return UI_HANDLER;
    }

    public static Executor newSerialExecutor() {
        return Executors.newSingleThreadExecutor(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Log.i(ThreadManagerV2.TAG, "serialExecutor_thread");
                return new Thread(runnable, "serialExecutor_thread");
            }
        });
    }

    public static Executor getNetExcutor() {
        return mNetExcutorPool;
    }

    public static Thread getRecentThread() {
        if (RECENT_THREAD == null) {
            getRecentThreadLooper();
        }
        return RECENT_THREAD;
    }

    public static Looper getRecentThreadLooper() {
        if (RECENT_THREAD_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                RECENT_THREAD = newFreeHandlerThread("Recent_Handler", 0);
                RECENT_THREAD.start();
                RECENT_THREAD_HANDLER = new Handler(RECENT_THREAD.getLooper());
            }
        }
        return RECENT_THREAD_HANDLER.getLooper();
    }

    public static Timer getTimer() {
        if (TIMER == null) {
            synchronized (ThreadManagerV2.class) {
                TIMER = new Timer("QQ_Timer") {
                    public void cancel() {
                        Log.i(ThreadManagerV2.TAG, "Can't cancel Global Timer");
                        if (!ThreadSetting.isPublicVersion) {
                            throw new RuntimeException("Can't cancel Global Timer");
                        }
                    }

                    public void schedule(TimerTask task, long delay) {
                        try {
                            super.schedule(task, delay);
                        } catch (Exception ex) {
                            Log.i(ThreadManagerV2.TAG, "timer schedule err", ex);
                        }
                    }

                    public void schedule(TimerTask task, long delay, long period) {
                        try {
                            super.schedule(task, delay, period);
                        } catch (Exception ex) {
                            Log.i(ThreadManagerV2.TAG, "timer schedule2 err", ex);
                        }
                    }
                };
            }
        }
        return TIMER;
    }

    public static void executeOnSubThread(Runnable run, boolean canAutoRecycled) {
        getSubThreadHandlerV2().post(run);
    }
}
