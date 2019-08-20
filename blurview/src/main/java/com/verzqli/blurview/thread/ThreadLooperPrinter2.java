package com.verzqli.blurview.thread;


import android.os.SystemClock;
import android.util.Printer;

class ThreadLooperPrinter2 implements Printer {
    public static final int DEFAULT_THRESHOLD_TIME = 200;
    public static final int FAMILY_DISPATCHER_TASK = 2;
    public static final int FAMILY_FILW_TASK = 3;
    public static final int FAMILY_SUB_TASK = 1;
    public static final String START_PREFIX = ">>";
    public static final String STOP_PREFIX = "<<";
    private static final String TAG = "TM.global.LooperPrinter";
    public static int sLogThreshold = 200;
    private String lastLog;
    private int mFamily = 0;
    private String mLooperName;
    private long msgCount;
    private int notReortedLoopCount = 0;
    private long startTime;
    private long totalCost;

    ThreadLooperPrinter2(int family, String LooperName) {
        this.mFamily = family;
        this.mLooperName = LooperName;
    }

    void setDebugSettings(int threshold, boolean toToast) {
        ThreadLog.printQLog(TAG, "setting threshold, threshold=" + threshold);
        sLogThreshold = threshold;
    }

    public void println(String x) {
        if (x.startsWith(START_PREFIX)) {
            this.startTime = SystemClock.uptimeMillis();
            this.lastLog = x;
        } else if (this.startTime != 0 && x.startsWith(STOP_PREFIX)) {
            this.msgCount++;
            long cost = SystemClock.uptimeMillis() - this.startTime;
            this.startTime = 0;
            this.totalCost += cost;
            if (ThreadSetting.logcatBgTaskMonitor) {
                ThreadLog.printQLog(ThreadManagerV2.AUTO_MONITOR_TAG, this.mLooperName + ", cost=" + cost + ", " + format(this.lastLog));
            } else if (cost >= ((long) sLogThreshold)) {
                ThreadLog.printQLog(ThreadManagerV2.AUTO_MONITOR_TAG, this.mLooperName + " OOT cost=" + cost + ", " + format(this.lastLog));
            }
        }
    }

    private static String format(String src) {
        if (src == null || src.length() == 0 || !src.startsWith(">>>")) {
            return null;
        }
        int substrBegin = src.indexOf(40);
        if (substrBegin == -1) {
            return null;
        }
        int substrEnd = src.indexOf(41, substrBegin);
        if (substrEnd == -1) {
            return null;
        }
        String handler = src.substring(substrBegin + 1, substrEnd);
        substrBegin = src.indexOf("} ", substrEnd);
        if (substrBegin == -1) {
            return null;
        }
        substrEnd = src.indexOf(64, substrBegin + 2);
        if (substrEnd == -1) {
            substrEnd = src.indexOf(58, substrBegin + 2);
            if (substrEnd == -1) {
                substrEnd = src.indexOf(32, substrBegin + 2);
                if (substrEnd == -1) {
                    return null;
                }
            }
        }
        String callback = src.substring(substrBegin + 2, substrEnd);
        substrBegin = src.indexOf(": ", substrEnd);
        if (substrBegin == -1) {
            return null;
        }
        String msgId = src.substring(substrBegin + 2);
        return String.format("\"%s|%s|%s\"", new Object[]{handler, callback, msgId});
    }

    public String toString() {
        return super.toString() + "(msgCount = " + this.msgCount + ", totalCost = " + this.totalCost + ")";
    }
}