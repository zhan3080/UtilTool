package com.example.commonutil.mirror;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class ScreenCastService extends Service {
    private static final String TAG = "ScreenCastService";
    private static final String PACKEGE_NAME = "com.example.commonutil.mirror:ScreenCastService";
    private MediaProjection mMediaProjection = null;
    private PowerManager.WakeLock mWakeLock;
    //    private ToastHandler mHandler;
    private WindowManager mWManager;
    private WindowManager.LayoutParams mWmParams;
    private TextView mView;
    private ScreenCast mScreenCast;
    private boolean isFirst = true;
    private boolean isOn = false;
    private int mHeight;
    private int mWidth;
    public static final int STATE_OFF = 1;
    public static final int STATE_INIT = 0;

    public static final String KEY_MIRROR_SWTICH = "mirrorSwtich";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate ");
        createView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy ");
        stopMirror();
    }

    private void createView() {
        mWManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        try {
            mWmParams = new WindowManager.LayoutParams();
            mWmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            mWmParams.format = 1;
            mWmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            mWmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mWmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mWmParams.horizontalMargin = 1920;
            mWmParams.verticalMargin = 1080;
            mView = new TextView(getApplicationContext());
            mView.setHeight(1);
            mView.setWidth(1);
            mView.setBackgroundColor(Color.TRANSPARENT);
            mWManager.addView(mView, mWmParams);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            if (isFirst) {
                isFirst = false;
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PACKEGE_NAME);
                mWakeLock.acquire();
            }
            serviceToForeground();
        }
        int swtich = intent.getIntExtra(KEY_MIRROR_SWTICH, -1);
        if (swtich == STATE_INIT) {
            serviceToForeground();
        } else if (swtich == STATE_OFF) {
            stopForeground(true);
            stopMirror();
        }
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }


    private static final String NTF_CHANNEL_WFD = "wfd_notification_channel";

    private void serviceToForeground() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel wfdChannel = new NotificationChannel(NTF_CHANNEL_WFD, NTF_CHANNEL_WFD, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(wfdChannel);
            Notification.Builder builder = new Notification.Builder(getApplicationContext(), NTF_CHANNEL_WFD);
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
            startForeground(Process.myPid(), notification);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            Notification notification = builder.build();
            notification.flags = Notification.FLAG_FOREGROUND_SERVICE;
            notification.defaults = Notification.DEFAULT_SOUND;
            startForeground(Process.myPid(), notification);
        }

    }

    public void setMediaProjection(MediaProjection mediaProjection) {
        this.mMediaProjection = mediaProjection;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new ScreenBinder();
    }

    public class ScreenBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public ScreenCastService getService() {
            return ScreenCastService.this;
        }
    }

    private void stopMirror() {
        Log.i(TAG,"stopMirror ");
//        stopAllStateCheck();
        if (mScreenCast != null) {
            mScreenCast.release();
            mScreenCast = null;
        }
    }

    public void startMirror() {
        if (mMediaProjection == null) {
            Log.i(TAG,"startMirror fail mMediaProjection is null");
            return;
        }
        stopMirror();
        Log.i(TAG,"startMirror ");
        mScreenCast = new ScreenCast(mMediaProjection);

        mScreenCast.start();
    }
}
