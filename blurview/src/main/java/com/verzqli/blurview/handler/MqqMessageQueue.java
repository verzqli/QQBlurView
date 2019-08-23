package com.verzqli.blurview.handler;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Printer;


public class MqqMessageQueue implements Callback, IdleHandler {
    private static boolean DEBUG_QUEUE = false;
    private static final int MSG_HOOK = 1000;
    private static final int MSG_IDLE_TIMEOUT = 1001;
    private static final String TAG = "MqqMessage.Queue";
    private static MqqMessageQueue sSubMainQueue;
    private volatile boolean hookReqeusted;
    private volatile boolean idleHandlerAttached;
    Handler mHandler;
    Printer mLogging;
    MqqMessage mMessages;
    long msgCount = 0;
    long totalCost = 0;

    public static synchronized MqqMessageQueue getSubMainThreadQueue() {
        MqqMessageQueue mqqMessageQueue;
        synchronized (MqqMessageQueue.class) {
            if (sSubMainQueue == null) {
                sSubMainQueue = new MqqMessageQueue(Looper.getMainLooper());
            }
            mqqMessageQueue = sSubMainQueue;
        }
        return mqqMessageQueue;
    }

    private MqqMessageQueue(Looper looper) {
        this.mHandler = new Handler(looper, this) {
            public String toString() {
                return "MessageQueueHandler";
            }
        };
    }

    public void setMessageLogging(Printer printer) {
        this.mLogging = printer;
    }

    public void setDetailLogging(boolean open) {
        DEBUG_QUEUE = open;
        MqqMessage.DEBUG_MESSAGE = open;
    }

    boolean enqueueMessage(MqqMessage msg, long when) {
        if (DEBUG_QUEUE) {
            Log.d(TAG, "enqueueMessage: " + msg.toString());
        }
        synchronized (this) {
            msg.when = when;
            MqqMessage p = this.mMessages;
            if (p == null || when == 0 || when < p.when) {
                msg.next = p;
                this.mMessages = msg;
            } else {
                MqqMessage prev = null;
                while (p != null && p.when <= when) {
                    prev = p;
                    p = p.next;
                }
                msg.next = prev.next;
                prev.next = msg;
            }
            reqHookIdleHandler();
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final MqqMessage next() {
        /*
        r12 = this;
        r6 = 0;
        r1 = 0;
        monitor-enter(r12);
        r2 = android.os.SystemClock.uptimeMillis();	 Catch:{ all -> 0x0038 }
        r0 = r12.mMessages;	 Catch:{ all -> 0x0038 }
        if (r0 == 0) goto L_0x0036;
    L_0x000b:
        r4 = r0.when;	 Catch:{ all -> 0x0038 }
        r7 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
        if (r7 < 0) goto L_0x001a;
    L_0x0011:
        r6 = r0.next;	 Catch:{ all -> 0x0038 }
        r12.mMessages = r6;	 Catch:{ all -> 0x0038 }
        r6 = 0;
        r0.next = r6;	 Catch:{ all -> 0x0038 }
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
    L_0x0019:
        return r0;
    L_0x001a:
        r8 = r4 - r2;
        r10 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        r8 = java.lang.Math.min(r8, r10);	 Catch:{ all -> 0x0038 }
        r1 = (int) r8;	 Catch:{ all -> 0x0038 }
        r7 = r12.mHandler;	 Catch:{ all -> 0x0038 }
        r8 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r7.removeMessages(r8);	 Catch:{ all -> 0x0038 }
        r7 = r12.mHandler;	 Catch:{ all -> 0x0038 }
        r8 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r10 = (long) r1;	 Catch:{ all -> 0x0038 }
        r7.sendEmptyMessageDelayed(r8, r10);	 Catch:{ all -> 0x0038 }
    L_0x0033:
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        r0 = r6;
        goto L_0x0019;
    L_0x0036:
        r1 = -1;
        goto L_0x0033;
    L_0x0038:
        r6 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x0038 }
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: mqq.os.MqqMessageQueue.next():mqq.os.MqqMessage");
    }

    void removeCallbacksAndMessages(MqqHandler h, Object object) {
        synchronized (this) {
            MqqMessage n;
            MqqMessage p = this.mMessages;
            while (p != null && p.target == h && (object == null || p.wrappedMsg.obj == object)) {
                n = p.next;
                this.mMessages = n;
                printDeletionLog(p);
                p.recycle();
                p = n;
            }
            while (p != null) {
                n = p.next;
                if (n != null && n.target == h && (object == null || n.wrappedMsg.obj == object)) {
                    MqqMessage nn = n.next;
                    printDeletionLog(n);
                    n.recycle();
                    p.next = nn;
                } else {
                    p = n;
                }
            }
        }
    }

    final void removeMessages(MqqHandler h, Runnable r, Object object) {
        if (r != null) {
            synchronized (this) {
                MqqMessage n;
                MqqMessage p = this.mMessages;
                while (p != null && p.target == h && p.wrappedMsg.getCallback() == r && (object == null || p.wrappedMsg.obj == object)) {
                    n = p.next;
                    this.mMessages = n;
                    printDeletionLog(p);
                    p.recycle();
                    p = n;
                }
                while (p != null) {
                    n = p.next;
                    if (n != null && n.target == h && n.wrappedMsg.getCallback() == r && (object == null || n.wrappedMsg.obj == object)) {
                        MqqMessage nn = n.next;
                        printDeletionLog(n);
                        n.recycle();
                        p.next = nn;
                    } else {
                        p = n;
                    }
                }
            }
        }
    }

    boolean removeMessages(MqqHandler h, int what, Object object, boolean doRemove) {
        synchronized (this) {
            MqqMessage n;
            MqqMessage p = this.mMessages;
            boolean found = false;
            while (p != null && p.target == h && p.wrappedMsg.what == what && (object == null || p.wrappedMsg.obj == object)) {
                if (doRemove) {
                    found = true;
                    n = p.next;
                    this.mMessages = n;
                    printDeletionLog(p);
                    p.recycle();
                    p = n;
                } else {
                    return true;
                }
            }
            while (p != null) {
                n = p.next;
                if (n == null || n.target != h || n.wrappedMsg.what != what || (object != null && n.wrappedMsg.obj != object)) {
                    p = n;
                } else if (doRemove) {
                    found = true;
                    MqqMessage nn = n.next;
                    printDeletionLog(n);
                    n.recycle();
                    p.next = nn;
                } else {
                    return true;
                }
            }
            return found;
        }
    }

    private void printDeletionLog(MqqMessage msg) {
        Log.d(TAG, "removeMsg: " + msg.toString());
    }

    private void throwException(final Throwable e) {
        new Thread() {
            public void run() {
                throw new RuntimeException("queueIdle encounter business crash. " + Log.getStackTraceString(e));
            }
        }.start();
    }

    public boolean queueIdle() {
        this.mHandler.removeMessages(1001);
        boolean continueHook = dequeue(true);
        if (continueHook) {
            this.mHandler.sendEmptyMessage(1000);
        } else {
            this.idleHandlerAttached = false;
        }
        return continueHook;
    }

    private final void onQueueIdleTimeout() {
        if (dequeue(false)) {
            this.mHandler.sendEmptyMessage(1001);
        }
    }

    private boolean dequeue(boolean isIdle) {
        Log.d(TAG, "enter dequeue, idle = " + isIdle);
        MqqMessage msg = next();
        String msgContent = msg != null ? msg.toString() : "null";
        if (msg != null) {
            try {
                StringBuilder sb;
                if (this.mLogging != null) {
                    sb = new StringBuilder(256);
                    sb.append(">>>>> Dispatching to ");
                    sb.append(msg.target);
                    sb.append(" ");
                    sb.append(msg.wrappedMsg.getCallback());
                    sb.append(": ");
                    sb.append(msg.wrappedMsg.what);
                    this.mLogging.println(sb.toString());
                }
                long time = SystemClock.uptimeMillis();
                msg.target.dispatchMessage(msg.wrappedMsg);
                this.totalCost += SystemClock.uptimeMillis() - time;
                this.msgCount++;
                if (this.mLogging != null) {
                    sb = new StringBuilder(256);
                    sb.append("<<<<< Finished to ");
                    sb.append(msg.target);
                    sb.append(" ");
                    sb.append(msg.wrappedMsg.getCallback());
                    this.mLogging.println(sb.toString());
                }
                msg.recycle();
            } catch (Throwable e) {
                throwException(e);
            }
        }
        if (DEBUG_QUEUE && this.msgCount % 100 == 0) {
            Log.d(TAG, "dequeue|" + Long.valueOf(this.msgCount) + "|" + Long.valueOf(this.totalCost));
        }
        if (msg != null) {
            if (DEBUG_QUEUE) {
                Log.d(TAG, "dequeue, msg = " + msgContent);
            }
            return true;
        }
        if (DEBUG_QUEUE) {
            Log.d(TAG, "dequeue, msg = null");
        }
        return false;
    }

    private void reqHookIdleHandler() {
        Log.d(TAG, "reqHook, attached = " + Boolean.valueOf(this.idleHandlerAttached) + ", requested = " + Boolean.valueOf(this.hookReqeusted));
        if (this.idleHandlerAttached || this.hookReqeusted) {
            this.mHandler.sendEmptyMessageDelayed(1001, 1000);
            return;
        }
        this.hookReqeusted = true;
        this.mHandler.sendEmptyMessage(1000);
    }

    private void cancelHookReq() {
        this.mHandler.removeMessages(1000);
        this.hookReqeusted = false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "handleMessage, what = " + Integer.valueOf(msg.what) + ", attached = " + Boolean.valueOf(this.idleHandlerAttached));
        if (msg.what == 1000) {
            if (!this.idleHandlerAttached) {
                cancelHookReq();
                this.idleHandlerAttached = true;
                this.mHandler.getLooper().getQueue().addIdleHandler(this);
            }
            this.mHandler.sendEmptyMessageDelayed(1001, 1000);
        } else if (msg.what == 1001) {
            onQueueIdleTimeout();
        }
        return true;
    }
}