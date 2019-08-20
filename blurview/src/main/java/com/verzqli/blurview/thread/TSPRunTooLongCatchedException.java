package com.verzqli.blurview.thread;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class TSPRunTooLongCatchedException extends RuntimeException {
    public TSPRunTooLongCatchedException(String msg) {
        super(msg);
    }
}