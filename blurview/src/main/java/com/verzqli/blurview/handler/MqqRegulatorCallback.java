package com.verzqli.blurview.handler;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/20
 *     desc  :
 * </pre>
 */

public interface MqqRegulatorCallback {
    void checkInRegulatorMsg();

    boolean regulatorThread(Thread thread);
}