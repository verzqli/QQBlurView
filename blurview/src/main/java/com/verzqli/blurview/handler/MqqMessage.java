package com.verzqli.blurview.handler;

import android.os.Message;

import java.util.concurrent.atomic.AtomicInteger;

public class MqqMessage {
    static boolean DEBUG_MESSAGE = true;
    private static final int MAX_POOL_SIZE = 10;
    private static AtomicInteger sIndex = new AtomicInteger(0);
    private static MqqMessage sPool;
    private static int sPoolSize;
    private static final Object sPoolSync = new Object();
    private int index;
    MqqMessage next;
    IMqqMessageCallback target;
    long when;
    Message wrappedMsg;

    private MqqMessage() {
        if (DEBUG_MESSAGE) {
            this.index = sIndex.incrementAndGet();
        }
    }

    public static MqqMessage obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                MqqMessage m = sPool;
                sPool = m.next;
                m.next = null;
                if (DEBUG_MESSAGE) {
                    m.index = sIndex.incrementAndGet();
                }
                sPoolSize--;
                return m;
            }
            return new MqqMessage();
        }
    }

    public static MqqMessage obtain(Message orginalMsg) {
        MqqMessage msg = obtain();
        msg.wrappedMsg = orginalMsg;
        return msg;
    }

    public void recycle() {
        if (this.wrappedMsg != null) {
            this.wrappedMsg.recycle();
        }
        this.when = 0;
        this.wrappedMsg = null;
        this.target = null;
        synchronized (sPoolSync) {
            if (sPoolSize < 10) {
                this.next = sPool;
                sPool = this;
                sPoolSize++;
            }
        }
    }

    public String toString() {
        return "MqqMessage@" + this.index;
    }
}