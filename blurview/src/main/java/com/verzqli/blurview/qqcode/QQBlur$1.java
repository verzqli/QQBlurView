//package com.tencent.mobileqq.widget;
//
//import android.graphics.Bitmap;
//import android.os.SystemClock;
//import android.view.View;
//import com.enrique.stackblur.StackBlurManager;
//import com.tencent.qphone.base.util.QLog;
//import defpackage.azlc;
//
///* compiled from: ProGuard */
//public class QQBlur$1 implements Runnable {
//    private int a = -1;
//    /* renamed from: a */
//    final /* synthetic */ StackBlurManager f541a;
//    final /* synthetic */ azlc this$0;
//
//    public QQBlur$1(azlc azlc, StackBlurManager stackBlurManager) {
//        this.this$0 = azlc;
//        this.f541a = stackBlurManager;
//    }
//
//    public void run() {
//        if (!this.this$0.f531b) {
//            long elapsedRealtime = SystemClock.elapsedRealtime();
//            if (!(this.a == -1 || this.a == azlc.a)) {
//                this.this$0.a(this.a, azlc.a);
//            }
//            this.a = azlc.a;
//            int i = azlc.a;
//            Bitmap process = this.f541a.process(this.this$0.f531b);
//            if (process != null) {
//                this.this$0.f519a = process;
//            } else {
//                QLog.e("QQBlur", 1, "run: outBitmap is null. OOM ?");
//            }
//            long elapsedRealtime2 = SystemClock.elapsedRealtime();
//            this.this$0.f531b;
//            this.this$0.f = (elapsedRealtime2 - elapsedRealtime) + this.this$0.f;
//            View a = this.this$0.f531b;
//            if (a != null && this.this$0.f) {
//                a.postInvalidate();
//            }
//        }
//    }
//}