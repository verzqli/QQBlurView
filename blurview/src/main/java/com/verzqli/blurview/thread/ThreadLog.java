package com.verzqli.blurview.thread;
import android.os.Handler;
import android.os.Looper;

public class ThreadLog {
    public static void printQLog(String tag, String msg) {
        printQLog(tag, msg, null);
    }

    public static void printQLog(String tag, String msg, Throwable tr) {
        if (isColorLevel()) {
            ThreadManagerV2.sThreadWrapContext.d(tag, ThreadSetting.CLR, msg, tr);
        }
    }

    public static void trackException(final String tag, String exceptionMsg) {
        final IllegalArgumentException ex = new IllegalArgumentException(exceptionMsg);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                ThreadLog.printQLog(tag, "ExceptinTracker", ex);
                throw new IllegalArgumentException(ex);
            }
        });
        throw ex;
    }

    public static boolean isColorLevel() {
        return ThreadManagerV2.sThreadWrapContext != null && ThreadManagerV2.sThreadWrapContext.isColorLevel();
    }

    public static boolean needRecordJob() {
        if (needReportRunOrBlocking()) {
            return true;
        }
        if (ThreadManagerV2.sThreadWrapContext != null) {
            return ThreadManagerV2.sThreadWrapContext.isShotReportRejectedError();
        }
        return false;
    }

    public static boolean needReportRunOrBlocking() {
        return !ThreadSetting.isPublicVersion && ThreadSetting.sProcessId == ThreadSetting.PROCESS_QQ;
    }
}