package com.verzqli.blurview.thread;


import android.os.Looper;

import com.verzqli.blurview.handler.MqqHandler;


/* compiled from: ProGuard */
public class ThreadManager extends ThreadManagerV2 {
    private static volatile MqqHandler FILE_Mqq_HANDLER;
    private static volatile MqqHandler SUB_Mqq_HANDLER;
    private static volatile MqqHandler UI_Mqq_HANDLER;

    public static MqqHandler getSubThreadHandler() {
        if (SUB_Mqq_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                if (SUB_Mqq_HANDLER == null) {
                    SUB_Mqq_HANDLER = new SubThreadHandler(ThreadManagerV2.getSubThreadLooper());
                }
            }
        }
        return SUB_Mqq_HANDLER;
    }

    public static MqqHandler getFileThreadHandler() {
        if (FILE_Mqq_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                if (FILE_Mqq_HANDLER == null) {
                    FILE_Mqq_HANDLER = new FileThreadHandler(ThreadManagerV2.getFileThreadLooper());
                }
            }
        }
        return FILE_Mqq_HANDLER;
    }

    public static MqqHandler getUIHandler() {
        if (UI_Mqq_HANDLER == null) {
            synchronized (ThreadManagerV2.class) {
                if (UI_Mqq_HANDLER == null) {
                    UI_Mqq_HANDLER = new MqqHandler(Looper.getMainLooper(), null, true);
                }
            }
        }
        return UI_Mqq_HANDLER;
    }

    public static void initDPC() {
    }
}