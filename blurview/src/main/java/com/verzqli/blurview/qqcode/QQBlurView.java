//package com.tencent.mobileqq.widget;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.graphics.Canvas;
//import android.graphics.drawable.Drawable;
//import android.support.annotation.Nullable;
//import android.util.AttributeSet;
//import android.view.View;
//import android.view.ViewTreeObserver.OnPreDrawListener;
//import com.tencent.qphone.base.util.QLog;
//import defpackage.azlc;
//import defpackage.azld;
//import defpackage.azle;
//import defpackage.azlf;
//
//@TargetApi(19)
///* compiled from: ProGuard */
//public class QQBlurView extends View {
//    private Drawable a;
//    /* renamed from: a */
//    private OnPreDrawListener f511a = new azlf(this);
//    /* renamed from: a */
//    private azlc f512a = new azlc();
//    /* renamed from: a */
//    private boolean f513a = true;
//
//    public QQBlurView(Context context) {
//        super(context);
//        e();
//    }
//
//    public QQBlurView(Context context, @Nullable AttributeSet attributeSet) {
//        super(context, attributeSet);
//        e();
//    }
//
//    public QQBlurView(Context context, @Nullable AttributeSet attributeSet, int i) {
//        super(context, attributeSet, i);
//        e();
//    }
//
//    private void e() {
//    }
//
//    protected void onDraw(Canvas canvas) {
//        if (!a()) {
//            if (this.f513a) {
//                setBackgroundDrawable(null);
//                this.f512a.a((View) this, canvas);
//                super.onDraw(canvas);
//                return;
//            }
//            setBackgroundDrawable(this.a);
//            super.onDraw(canvas);
//        }
//    }
//
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        if (this.f512a != null) {
//            a();
//        }
//    }
//
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        if (this.f512a != null) {
//            b();
//        }
//    }
//
//    public void a() {
//        if (QLog.isColorLevel()) {
//            QLog.d("QQBlurView", 2, "onResume() called");
//        }
//        this.f512a.b();
//    }
//
//    public void b() {
//        if (QLog.isColorLevel()) {
//            QLog.d("QQBlurView", 2, "onPause() called");
//        }
//        this.f512a.a();
//    }
//
//    public void c() {
//        getViewTreeObserver().removeOnPreDrawListener(this.f511a);
//        this.f512a.c();
//    }
//
//    public void a(View view) {
//        this.f512a.a(view);
//    }
//
//    public void b(View view) {
//        this.f512a.b(view);
//    }
//
//    public void a(azle azle) {
//        this.f512a.a(azle);
//    }
//
//    public void d() {
//        getViewTreeObserver().addOnPreDrawListener(this.f511a);
//        this.f512a.a();
//    }
//
//    public void a(Drawable drawable) {
//        this.f512a.a(drawable);
//    }
//
//    public void a(float f) {
//        this.f512a.a(f);
//    }
//
//    public void a(int i) {
//        this.f512a.a(i);
//    }
//
//    public void setDirtyListener(azld azld) {
//        this.f512a.a(azld);
//    }
//
//    public void draw(Canvas canvas) {
//        if (a()) {
//            this.f512a.d();
//        } else {
//            super.draw(canvas);
//        }
//    }
//
//    protected void dispatchDraw(Canvas canvas) {
//        if (!a()) {
//            super.dispatchDraw(canvas);
//        }
//    }
//
//    public void setDebugTag(String str) {
//        this.f512a.a(str);
//    }
//
//    public void b(int i) {
//        this.f512a.b(i);
//    }
//
//    public void setEnableBlur(boolean z) {
//        this.f513a = z;
//    }
//
//    public void setDisableBlurDrawableRes(int i) {
//        this.a = getResources().getDrawable(i);
//    }
//
//    public void c(int i) {
//        this.f512a.c(i);
//    }
//
//    /* renamed from: a */
//    public boolean m99a() {
//        return this.f512a.b();
//    }
//}