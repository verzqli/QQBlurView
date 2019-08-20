package com.verzqli.blurview.thread;

import java.util.HashMap;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public interface ThreadWrapContext {
    void d(String str, int i, String str2, Throwable th);

    long getMainProccessThreadMonitorTime();

    long getMainProccessThreadPeakCounts();

    boolean isColorLevel();

    boolean isShotReportRejectedError();

    void reportDengTaException(String str, String str2, boolean z, long j, long j2, HashMap<String, String> hashMap, String str3, boolean z2);

    void reportRDMException(Throwable th, String str, String str2);

    void setMainProccessThreadMonitorTime(long j);

    void setMainProccessThreadPeakCounts(long j);
}