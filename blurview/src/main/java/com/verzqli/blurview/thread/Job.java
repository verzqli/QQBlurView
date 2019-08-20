package com.verzqli.blurview.thread;

import android.os.SystemClock;

import com.verzqli.blurview.thread.ThreadExcutor.IThreadListener;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class Job  extends WeakReference<Object> implements Runnable, Comparable<Job> {
    private static int RUNNING_TIME_OUT = 9990000;
    private static final String TAG = "Job";
    public static ConcurrentLinkedQueue<String> runningJmapInAync = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInDB = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInDownload = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInFile = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInHeavy = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInLight = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInNet = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInNormal = new ConcurrentLinkedQueue();
    public static ConcurrentLinkedQueue<String> runningJmapInOther = new ConcurrentLinkedQueue();
    public long addPoint = 0;
    public long blcokingCost = -1;
    private boolean canAutoRetrieve;
    public long cost = -1;
    private boolean hasKey = false;
    public long mId = 0;
    public Runnable mJob;
    public IThreadListener mListener;
    public String mName;
    public int mType = 0;
    public int poolNum = -1;
    public long postCost = -1;
    public long wait = -1;

    public Job(Object key, Runnable job, boolean canAutoRetrieve) {
        super(key);
        if (key != null) {
            this.hasKey = true;
        }
        this.mJob = job;
        this.canAutoRetrieve = canAutoRetrieve;
    }

    Job(Object key, String name, int type, Runnable job, IThreadListener listener, boolean canAutoRetrieve) {
        super(key);
        if (key != null) {
            this.hasKey = true;
        }
        this.mName = job.toString();
        this.mType = type;
        this.mJob = job;
        this.mListener = listener;
        if (this.mListener != null) {
            this.mListener.onAdded();
        }
        this.addPoint = SystemClock.uptimeMillis();
        this.canAutoRetrieve = canAutoRetrieve;
    }

    public boolean checkShouldRun() {
        if (!this.canAutoRetrieve) {
            return true;
        }
        if (!this.hasKey) {
            return true;
        }
        Object obj = get();
        if (obj != null) {
            try {
                Field f = this.mJob.getClass().getDeclaredField("this$0");
                f.setAccessible(true);
                f.set(this.mJob, obj);
                return true;
            } catch (NoSuchFieldException e) {
                ThreadLog.printQLog(TAG, this.mName, e);
                return false;
            } catch (IllegalArgumentException e2) {
                ThreadLog.printQLog(TAG, this.mName, e2);
                return false;
            } catch (IllegalAccessException e3) {
                ThreadLog.printQLog(TAG, this.mName, e3);
                return false;
            }
        }
        ThreadLog.printQLog(TAG, this.mName + " never run, becuse outer object is retrieve already");
        return false;
    }

    private void beforeRun() {
        this.wait = SystemClock.uptimeMillis() - this.addPoint;
        JobReporter.reportJobTime(this.wait);
        if (this.mListener != null) {
            this.mListener.onPreRun();
        }
        if (ThreadSetting.logcatBgTaskMonitor) {
            ThreadLog.printQLog(ThreadManagerV2.TAG, "tsp execute|" + toString());
        }
        if (ThreadLog.needRecordJob()) {
            switch (this.poolNum) {
                case 1:
                    runningJmapInLight.add(this.mName);
                    return;
                case 2:
                    runningJmapInHeavy.add(this.mName);
                    return;
                case 5:
                    runningJmapInDownload.add(this.mName);
                    return;
                case 6:
                    runningJmapInNormal.add(this.mName);
                    return;
                case 7:
                    runningJmapInDB.add(this.mName);
                    return;
                case 8:
                    runningJmapInFile.add(this.mName);
                    return;
                case 9:
                    runningJmapInNet.add(this.mName);
                    return;
                case 10:
                    runningJmapInAync.add(this.mName);
                    return;
                case 11:
                    runningJmapInOther.add(this.mName);
                    return;
                default:
                    return;
            }
        }
    }

    private void afterRun() {
        this.cost = SystemClock.uptimeMillis() - (this.wait + this.addPoint);
        if (this.mListener != null) {
            this.mListener.onPostRun();
        }
        reportRunningTooLong();
        if (ThreadSetting.logcatBgTaskMonitor) {
            ThreadLog.printQLog(ThreadManagerV2.TAG, "tsp execute-" + toString());
        }
        if (ThreadLog.needRecordJob()) {
            switch (this.poolNum) {
                case 1:
                    runningJmapInLight.remove(this.mName);
                    return;
                case 2:
                    runningJmapInHeavy.remove(this.mName);
                    return;
                case 5:
                    runningJmapInDownload.remove(this.mName);
                    return;
                case 6:
                    runningJmapInNormal.remove(this.mName);
                    return;
                case 7:
                    runningJmapInDB.remove(this.mName);
                    return;
                case 8:
                    runningJmapInFile.remove(this.mName);
                    return;
                case 9:
                    runningJmapInNet.remove(this.mName);
                    return;
                case 10:
                    runningJmapInAync.remove(this.mName);
                    return;
                case 11:
                    runningJmapInOther.remove(this.mName);
                    return;
                default:
                    return;
            }
        }
    }

    private void reportRunningTooLong() {
        if (ThreadLog.needReportRunOrBlocking() && this.cost >= get_RUNNING_TIME_OUT() && ThreadManagerV2.OPEN_RDM_REPORT && ThreadManagerV2.sThreadWrapContext != null) {
            String TagName = "max_reportJobRunningTooLong";
            StringBuilder sb = new StringBuilder();
            sb.append("process_" + ThreadSetting.sProcessId).append(" mjobName_" + this.mName).append(" mType_" + this.mType).append(" cost_" + this.cost);
            ThreadLog.printQLog(TAG, sb.toString());
            ThreadManagerV2.sThreadWrapContext.reportRDMException(new TSPRunTooLongCatchedException(TagName), TagName, sb.toString());
        }
    }

    private static long get_RUNNING_TIME_OUT() {
        if (!ThreadSetting.isPublicVersion) {
            RUNNING_TIME_OUT = 60000;
        }
        return (long) RUNNING_TIME_OUT;
    }

    public void run() {
        if (checkShouldRun()) {
            beforeRun();
            this.mJob.run();
            afterRun();
            return;
        }
        ThreadLog.printQLog(TAG, this.mName + " is recycled");
    }

    public int hashCode() {
        return (this.mJob == null ? 0 : this.mJob.hashCode()) + 31;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Job other = (Job) obj;
        if (this.mJob == null) {
            if (other.mJob != null) {
                return false;
            }
            return true;
        } else if (this.mJob.equals(other.mJob)) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(" cost=").append(this.cost).append(", ").append(this.mName).append("|pool-").append(this.poolNum).append("|t-id=").append(this.mId).append("|mType=").append(this.mType).append("|wait=").append(this.wait).append("|postCost=").append(this.postCost).append("|bCost=").append(this.blcokingCost);
        return sb.toString();
    }

    public int compareTo(Job o) {
        if (this.mType == o.mType) {
            return 0;
        }
        return this.mType > o.mType ? -1 : 1;
    }
}