package com.verzqli.blurview.thread;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */

public class JobReporter {
    private static final int LEVEL_COUNT = 3;
    private static final int MSG_PUT_INTO_THRED_LIST = 1;
    private static final int MSG_REPORT_THRED_PEAK = 2;
    private static final int MSG_THRED_CREATE_CHECK = 3;
    private static final int REPORT_LEVEL_SEPARATOR = 500;
    private static final int REPORT_THRESHOLD = 200;
    private static final String TAG = "JobReporter";
    private static final long THREAD_COUNT_REPORT_INTERVAL_Debug = 20000;
    private static final long THREAD_COUNT_REPORT_INTERVAL_Release = 86400000;
    private static final String ThreadMonitorPeakCount = "thread_monitor_peak_count";
    public static final String ThreadOnCreatedCallBack = "com/tencent/mobileqq/app/JobReporter";
    private static Handler mFileHandler = new Handler(ThreadManagerV2.getFileThreadLooper()) {
        public void handleMessage(Message msg) {
            if (msg.what == 1 && msg.obj != null) {
                JobReporter.weakThreadList.add(new WeakReference(msg.obj));
            } else if (msg.what == 2) {
                if (ThreadManagerV2.sThreadWrapContext != null) {
                    JobReporter.initThreadPeakCount();
                    long ctime = System.currentTimeMillis();
                    if (ctime - JobReporter.sMonitorStartTime <= (ThreadSetting.isPublicVersion ? 86400000 : JobReporter.THREAD_COUNT_REPORT_INTERVAL_Debug) || JobReporter.sThreadPeakCount <= 0 || !JobReporter.peakCountRamdomReport()) {
                        long count = (long) JobReporter.getCurrentThreadCount();
                        ThreadLog.printQLog(JobReporter.TAG, "saveThreadPeakCount count" + count + " sThreadPeakCount " + JobReporter.sThreadPeakCount);
                        if (count > JobReporter.sThreadPeakCount) {
                            JobReporter.sThreadPeakCount = count;
                            ThreadManagerV2.sThreadWrapContext.setMainProccessThreadPeakCounts(JobReporter.sThreadPeakCount);
                            return;
                        }
                        return;
                    }
                    if (JobReporter.sThreadPeakCount < 500) {
                        ThreadManagerV2.sThreadWrapContext.reportDengTaException((String) msg.obj, JobReporter.ThreadMonitorPeakCount, true, JobReporter.sThreadPeakCount, 1, null, "", false);
                        ThreadLog.printQLog(JobReporter.TAG, "reportThreadPeakCount Yes " + JobReporter.sThreadPeakCount);
                        JobReporter.sMonitorStartTime = ctime;
                        ThreadManagerV2.sThreadWrapContext.setMainProccessThreadMonitorTime(ctime);
                    }
                    JobReporter.sThreadPeakCount = 0;
                    ThreadManagerV2.sThreadWrapContext.setMainProccessThreadPeakCounts(JobReporter.sThreadPeakCount);
                }
            } else if (msg.what != 3) {
                super.handleMessage(msg);
            } else if (!ThreadSetting.isPublicVersion && JobReporter.mThreadCheck != null && msg.obj != null) {
                JobReporter.mThreadCheck.isLegalName((CheckParams) msg.obj);
            }
        }
    };
    public static ThreadCheck mThreadCheck;
    private static Field nativePeerF = null;
    private static boolean nativePeerGetFailed = false;
    private static boolean nativePeerReflectFailed = false;
    private static boolean sInited = false;
    private static long sMonitorStartTime = 0;
    private static Random sRandom = new Random();
    public static AtomicLong sThreadJobReportCountLevelOne = new AtomicLong(0);
    public static AtomicLong sThreadJobReportCountLevelThree = new AtomicLong(0);
    public static AtomicLong sThreadJobReportCountLevelTwo = new AtomicLong(0);
    public static AtomicLong sThreadJobReportLastReportTs = new AtomicLong(0);
    public static AtomicLong sThreadJobReportTotalCount = new AtomicLong(0);
    private static long sThreadPeakCount = 0;
    private static List<WeakReference<Thread>> weakThreadList = new LinkedList();

    public static class CheckParams {
        public String newThreadName = "";
        public StackTraceElement[] ste;
    }

    static void reportJobTime(long wait) {
        try {
            if (ThreadSetting.sProcessId == ThreadSetting.PROCESS_QQ) {
                sThreadJobReportTotalCount.incrementAndGet();
                if (wait > 200) {
                    switch (Math.min((int) (wait / 500), 2)) {
                        case 0:
                            sThreadJobReportCountLevelOne.incrementAndGet();
                            return;
                        case 1:
                            sThreadJobReportCountLevelTwo.incrementAndGet();
                            return;
                        case 2:
                            sThreadJobReportCountLevelThree.incrementAndGet();
                            return;
                        default:
                            return;
                    }
                }
            }
        } catch (Throwable throwable) {
            ThreadLog.printQLog(TAG, "reportJobTime error!!!  : ", throwable);
        }
    }

    public static void onThreadCreatedCallback(Object o) {
        if (o != null && (o instanceof Thread)) {
            Message msg = mFileHandler.obtainMessage(1);
            msg.obj = o;
            mFileHandler.sendMessage(msg);
            if (!ThreadSetting.isPublicVersion) {
                Thread newThread = (Thread) o;
                CheckParams params = new CheckParams();
                params.newThreadName = newThread.getName();
                params.ste = Thread.currentThread().getStackTrace();
                Message msg2 = mFileHandler.obtainMessage(3);
                msg2.obj = params;
                mFileHandler.sendMessage(msg2);
            }
        }
    }

    private static int getCurrentThreadCount() {
        Field nativePeerF = getNativePeerField();
        if (nativePeerF == null || nativePeerGetFailed) {
            weakThreadList.clear();
            return 0;
        }
        int size = weakThreadList.size();
        if (size > 1024) {
            ThreadLog.printQLog(TAG, "getCurrentThreadCount beyond 1024:" + size);
            if (ThreadManagerV2.sThreadWrapContext != null) {
                ThreadManagerV2.sThreadWrapContext.reportDengTaException("", "ThreadPeakCountOverLimit", true, (long) size, 0, null, "", false);
            }
            weakThreadList.clear();
            return 0;
        }
        int i;
        int count = 0;
        List<WeakReference<Thread>> weakThreadDelList = new ArrayList();
        for (i = 0; i < size; i++) {
            if (nativePeerGetFailed) {
                weakThreadList.clear();
                return 0;
            }
            WeakReference<Thread> tRef = (WeakReference) weakThreadList.get(i);
            Thread thread = (Thread) tRef.get();
            if (thread != null) {
                try {
                    if (((Long) nativePeerF.get(thread)).longValue() <= 0) {
                        weakThreadDelList.add(tRef);
                    } else {
                        count++;
                    }
                } catch (Exception e) {
                    ThreadLog.printQLog(TAG, "getCurrentThreadCoun nativePeer err ", e);
                    nativePeerGetFailed = true;
                    weakThreadList.clear();
                    return 0;
                }
            }
            weakThreadDelList.add(tRef);
        }
        int sum = weakThreadDelList.size();
        for (i = 0; i < sum; i++) {
            weakThreadList.remove(weakThreadDelList.get(i));
        }
        return count;
    }

    private static Field getNativePeerField() {
        if (nativePeerF != null || nativePeerReflectFailed) {
            return nativePeerF;
        }
        try {
            nativePeerF = Thread.class.getDeclaredField("nativePeer");
            nativePeerF.setAccessible(true);
            return nativePeerF;
        } catch (Exception e) {
            e.printStackTrace();
            nativePeerF = null;
            nativePeerReflectFailed = true;
            return null;
        }
    }

    public static void reportThreadPeakCount(String cuin) {
        Message msg = mFileHandler.obtainMessage(2);
        msg.obj = cuin;
        mFileHandler.sendMessage(msg);
    }

    private static void initThreadPeakCount() {
        if (!sInited && ThreadManagerV2.sThreadWrapContext != null) {
            sThreadPeakCount = ThreadManagerV2.sThreadWrapContext.getMainProccessThreadPeakCounts();
            sMonitorStartTime = ThreadManagerV2.sThreadWrapContext.getMainProccessThreadMonitorTime();
            ThreadLog.printQLog(TAG, "initThreadPeakCount sThreadPeakCount " + sThreadPeakCount + " sMonitorStartTime " + sMonitorStartTime);
            sInited = true;
        }
    }

    private static boolean peakCountRamdomReport() {
        if (!ThreadSetting.isPublicVersion) {
            return true;
        }
        if (ThreadSetting.isGrayVersion) {
            return ramdomReport(10);
        }
        return ramdomReport(10000);
    }

    public static boolean ramdomReport(int sample) {
        if (!ThreadSetting.isPublicVersion) {
            return true;
        }
        if (sample <= 0) {
            return false;
        }
        if (sRandom.nextInt(sample) != 0) {
            return false;
        }
        return true;
    }
}