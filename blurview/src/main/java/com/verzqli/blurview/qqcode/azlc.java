//package defpackage;
//
//import android.annotation.TargetApi;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Bitmap.Config;
//import android.graphics.BitmapShader;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.graphics.Region.Op;
//import android.graphics.Shader.TileMode;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.os.Build.VERSION;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.os.SystemClock;
//import android.view.View;
//import android.view.ViewGroup;
//import com.enrique.stackblur.StackBlurManager;
//import com.tencent.mobileqq.app.ThreadManagerV2;
//import com.tencent.mobileqq.theme.ThemeConstants;
//import com.tencent.mobileqq.widget.QQBlur$1;
//import com.tencent.qphone.base.util.QLog;
//import com.tencent.ttpic.util.VideoMaterialUtil;
//import java.lang.reflect.Field;
//import java.util.ArrayList;
//import java.util.List;
//
//@TargetApi(19)
///* compiled from: ProGuard */
///* renamed from: azlc */
//public class azlc {
//    public static int a = 0;
//    /* renamed from: a */
//    private static HandlerThread f514a = ThreadManagerV2.newFreeHandlerThread("QQBlur", -8);
//    /* renamed from: a */
//    private static Field f515a;
//    /* renamed from: a */
//    private float f516a = 8.0f;
//    /* renamed from: a */
//    private long f517a;
//    /* renamed from: a */
//    private Context f518a;
//    /* renamed from: a */
//    private volatile Bitmap f519a;
//    /* renamed from: a */
//    private Canvas f520a;
//    /* renamed from: a */
//    private Paint f521a;
//    /* renamed from: a */
//    private RectF f522a = new RectF();
//    /* renamed from: a */
//    private Drawable f523a = new ColorDrawable(Color.parseColor("#DAFAFAFC"));
//    /* renamed from: a */
//    private Handler f524a;
//    /* renamed from: a */
//    private View f525a;
//    /* renamed from: a */
//    private azld f526a;
//    /* renamed from: a */
//    private azle f527a;
//    /* renamed from: a */
//    private String f528a;
//    /* renamed from: a */
//    private List<View> f529a = new ArrayList();
//    /* renamed from: a */
//    private volatile boolean f530a = true;
//    private float b = 1.0f;
//    /* renamed from: b */
//    private int f531b = 6;
//    /* renamed from: b */
//    private long f532b;
//    /* renamed from: b */
//    private volatile View f533b;
//    /* renamed from: b */
//    private boolean f534b;
//    private float c = 1.0f;
//    /* renamed from: c */
//    private int f535c = 0;
//    /* renamed from: c */
//    private long f536c;
//    /* renamed from: c */
//    private boolean f537c;
//    private float d;
//    /* renamed from: d */
//    private int f538d = 2;
//    /* renamed from: d */
//    private long f539d;
//    private float e;
//    /* renamed from: e */
//    private long f540e;
//    private long f;
//    private long g;
//    private long h;
//    private long i;
//    private long j;
//    private long k;
//
//    static {
//        f514a.start();
//    }
//
//    private void e() {
//        long elapsedRealtime = SystemClock.elapsedRealtime();
//        if (this.f525a != null && this.f533b != null && this.f533b.getWidth() > 0 && this.f533b.getHeight() > 0) {
//            Bitmap createBitmap;
//            int a = azlc.a((float) this.f533b.getWidth(), this.f516a);
//            int a2 = azlc.a((float) this.f533b.getHeight(), this.f516a);
//            int a3 = azlc.a(a);
//            int a4 = azlc.a(a2);
//            this.c = ((float) a2) / ((float) a4);
//            this.b = ((float) a) / ((float) a3);
//            float f = this.f516a * this.b;
//            float f2 = this.f516a * this.c;
//            try {
//                createBitmap = Bitmap.createBitmap(a3, a4, Config.ARGB_8888);
//            } catch (Throwable e) {
//                QLog.e("QQBlur", 1, "prepareBlurBitmap: ", e);
//                createBitmap = null;
//            }
//            if (createBitmap != null) {
//                this.g = (long) createBitmap.getWidth();
//                this.h = (long) createBitmap.getHeight();
//                if (VERSION.SDK_INT >= 19) {
//                    this.i = (long) createBitmap.getAllocationByteCount();
//                } else {
//                    this.i = (long) createBitmap.getByteCount();
//                }
//                createBitmap.eraseColor(this.f535c);
//                this.f520a.setBitmap(createBitmap);
//                int[] iArr = new int[2];
//                this.f533b.getLocationInWindow(iArr);
//                int[] iArr2 = new int[2];
//                this.f525a.getLocationInWindow(iArr2);
//                this.f520a.save();
//                this.f520a.translate(((float) (-(iArr[0] - iArr2[0]))) / f, ((float) (-(iArr[1] - iArr2[1]))) / f2);
//                this.f520a.scale(1.0f / f, 1.0f / f2);
//                StackBlurManager stackBlurManager = new StackBlurManager(createBitmap);
//                stackBlurManager.setDbg(false);
//                stackBlurManager.setExecutorThreads(stackBlurManager.getExecutorThreads());
//                Bundle bundle = new Bundle();
//                if (this.f527a != null) {
//                    this.f527a.a(bundle);
//                }
//                this.f537c = true;
//                if (VERSION.SDK_INT <= 27 || this.f533b.getContext().getApplicationInfo().targetSdkVersion <= 27) {
//                    Rect clipBounds = this.f520a.getClipBounds();
//                    clipBounds.inset(-createBitmap.getWidth(), -createBitmap.getHeight());
//                    if (this.f520a.clipRect(clipBounds, Op.REPLACE)) {
//                        this.f525a.draw(this.f520a);
//                    } else {
//                        QLog.e("QQBlur", 1, "prepareBlurBitmap: canvas clip rect empty. Cannot draw!!!");
//                    }
//                } else {
//                    this.f525a.draw(this.f520a);
//                }
//                this.f520a.restore();
//                g();
//                this.f537c = false;
//                if (this.f527a != null) {
//                    this.f527a.b(bundle);
//                }
//                this.f524a.post(new QQBlur$1(this, stackBlurManager));
//            } else {
//                return;
//            }
//        }
//        long elapsedRealtime2 = SystemClock.elapsedRealtime();
//        this.f517a++;
//        this.f532b = (elapsedRealtime2 - elapsedRealtime) + this.f532b;
//    }
//
//    public azlc a(View view) {
//        this.f525a = view;
//        return this;
//    }
//
//    public azlc b(View view) {
//        this.f533b = view;
//        return this;
//    }
//
//    public azlc a() {
//        if (QLog.isColorLevel()) {
//            QLog.d("QQBlur", 2, "onCreate() called");
//        }
//        this.f518a = this.f533b.getContext();
//        this.f520a = new Canvas();
//        this.f524a = new Handler(f514a.getLooper());
//        this.f534b = true;
//        f();
//        return this;
//    }
//
//    private void f() {
//        if (this.f518a != null && this.f525a != null && this.f533b != null) {
//        }
//    }
//
//    public void a(View view, Canvas canvas) {
//        long elapsedRealtime = SystemClock.elapsedRealtime();
//        Bitmap bitmap = this.f519a;
//        if (bitmap != null) {
//            canvas.save();
//            canvas.scale((((float) view.getWidth()) * 1.0f) / ((float) bitmap.getWidth()), (((float) view.getHeight()) * 1.0f) / ((float) bitmap.getHeight()));
//            if (this.f521a == null) {
//                this.f521a = new Paint(1);
//            }
//            this.f521a.setShader(new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP));
//            this.f522a.set(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight());
//            canvas.drawRoundRect(this.f522a, this.d, this.e, this.f521a);
//            if (this.f523a != null) {
//                this.f523a.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
//                this.f523a.draw(canvas);
//            }
//            canvas.restore();
//        }
//        long elapsedRealtime2 = SystemClock.elapsedRealtime();
//        this.f536c++;
//        this.f539d = (elapsedRealtime2 - elapsedRealtime) + this.f539d;
//    }
//
//    private static int a(float f, float f2) {
//        return (int) Math.ceil((double) (f / f2));
//    }
//
//    public static int a(int i) {
//        return i % 16 == 0 ? i : (i - (i % 16)) + 16;
//    }
//
//    public azlc a(azle azle) {
//        this.f527a = azle;
//        return this;
//    }
//
//    /* renamed from: a */
//    public void m102a() {
//        this.f530a = true;
//        QLog.i("QQBlur." + this.f528a, 2, a());
//    }
//
//    /* renamed from: a */
//    private CharSequence m100a(int i) {
//        switch (i) {
//            case 1:
//                return "StackBlur.Native";
//            case 2:
//                return "StackBlur.RS";
//            case 3:
//                return "GaussBlur.RS";
//            default:
//                return "StackBlur.Java";
//        }
//    }
//
//    public void b() {
//        this.f530a = false;
//    }
//
//    public void c() {
//        if (QLog.isColorLevel()) {
//            QLog.d("QQBlur", 2, "onDestroy() called");
//        }
//        if (this.f534b) {
//            this.f534b = false;
//            this.f524a.removeCallbacksAndMessages(null);
//            this.f524a = null;
//            this.f525a = null;
//            this.f533b = null;
//            this.f520a.setBitmap(null);
//            this.f520a = null;
//            this.f521a = null;
//            this.f527a = null;
//            this.f518a = null;
//        }
//    }
//
//    /* renamed from: a */
//    public boolean m104a() {
//        boolean z = false;
//        if (this.f526a != null) {
//            z = this.f526a.a();
//        } else if (this.f525a != null) {
//            z = this.f525a.isDirty();
//        }
//        View view = this.f533b;
//        if (!this.f530a && z && view != null && view.getVisibility() == 0) {
//            e();
//            view.invalidate();
//        }
//        return true;
//    }
//
//    public void a(Drawable drawable) {
//        this.f523a = drawable;
//    }
//
//    public void a(float f) {
//        this.f516a = f;
//    }
//
//    /* renamed from: a */
//    public void m103a(int i) {
//        this.f531b = i;
//    }
//
//    public void a(azld azld) {
//        this.f526a = azld;
//    }
//
//    public void a(String str) {
//        this.f528a = str;
//    }
//
//    public void b(int i) {
//        a = i;
//    }
//
//    private void a(int i, int i2) {
//        if (QLog.isColorLevel()) {
//            QLog.d("QQBlur", 2, "onPolicyChange() called with: from = [" + i + "], to = [" + i2 + "]");
//        }
//        this.f517a = 0;
//        this.f532b = 0;
//        this.f540e = 0;
//        this.f = 0;
//    }
//
//    /* renamed from: a */
//    public String m101a() {
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("方案=").append(a(a)).append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("缩放倍数=").append(this.f516a).append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("模糊半径=").append(this.f531b).append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("尺寸=" + this.g + VideoMaterialUtil.CRAZYFACE_X + this.h).append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("空间=" + (this.i / 1000) + "KB").append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("并发数=" + this.f538d).append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("主线程采样=[" + String.format("%.2f", new Object[]{Float.valueOf(((float) this.f532b) / ((float) this.f517a))}) + "]ms").append(ThemeConstants.THEME_SP_SEPARATOR);
//        stringBuilder.append("后台线程处理=[" + String.format("%.2f", new Object[]{Float.valueOf(((float) this.f) / ((float) this.f540e))}) + "]ms");
//        return stringBuilder.toString();
//    }
//
//    public void c(int i) {
//        this.f535c = i;
//    }
//
//    /* renamed from: b */
//    public boolean m105b() {
//        return this.f537c;
//    }
//
//    public void d() {
//        this.f529a.clear();
//        a(this.f525a.getRootView(), this.f529a);
//    }
//
//    private void g() {
//        for (View view : this.f529a) {
//            if (view != null) {
//                a(view, 0);
//            }
//        }
//    }
//
//    private void a(View view, List<View> list) {
//        if (view != null && view.getVisibility() == 0) {
//            list.add(view);
//            a(view, 4);
//            if (view instanceof ViewGroup) {
//                ViewGroup viewGroup = (ViewGroup) view;
//                int childCount = viewGroup.getChildCount();
//                for (int i = 0; i < childCount; i++) {
//                    a(viewGroup.getChildAt(i), (List) list);
//                }
//            }
//        }
//    }
//
//    private void a(View view, int i) {
//        long uptimeMillis = SystemClock.uptimeMillis();
//        try {
//            if (f515a == null) {
//                f515a = View.class.getDeclaredField("mViewFlags");
//                f515a.setAccessible(true);
//            }
//            f515a.setInt(view, (f515a.getInt(view) & -13) | (i & 12));
//        } catch (Throwable e) {
//            QLog.e("QQBlur", 1, "setViewInvisible: ", e);
//        }
//        long uptimeMillis2 = SystemClock.uptimeMillis() - uptimeMillis;
//        if (this.j >= 100000) {
//            this.j = 0;
//            this.k = 0;
//        }
//        this.j++;
//        this.k = uptimeMillis2 + this.k;
//        if (this.j % 2000 != 0) {
//        }
//    }
//}