package com.verzqli.blurview.thread;

import android.os.Looper;
import android.util.Log;

import com.verzqli.blurview.handler.MqqHandler;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/20
 *     desc  :
 * </pre>
 */
public class FileThreadHandler extends MqqHandler {
    public FileThreadHandler(Looper looper) {
        super(looper);
    }

    public void removeCallbacksAndMessages(Object obj) {
        if (obj == null) {
            Log.e(ThreadManagerV2.TAG, "global fileHandler cannot excute removeCallbacksAndMessages");
        } else {
            super.removeCallbacksAndMessages(obj);
        }
    }
}