package com.example.commonutil.mirror;

import android.annotation.TargetApi;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenCast extends Thread {
    private static final String TAG = "ScreenCast";
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video
    private static final int FRAME_RATE = 60; // 30 fps private int mBitRate;
    private static final int IFRAME_INTERVAL = 5;// 30*60;// 5; // 10 seconds between
    protected static final int MSG_ON_STOP_CALLBACK = 100;
    protected static final int MSG_ON_START_CALLBACK = 101;
    protected static final int MSG_ON_ERROR_CALLBACK = 102;
    protected static final int MSG_RESIZE_SCREEN = 103;
    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec mEncoder;
    private Surface mSurface;
    private VideoEncoder mVideoEncoder;
    private VirtualDisplayCallback mVirtualDisplayCallback;
    private ListenerCallbackHandler mHandler;

    ScreenCast(MediaProjection mediaProjection) {
        mMediaProjection = mediaProjection;
        mHandler = new ListenerCallbackHandler();
    }

    @Override
    public void run() {
        super.run();
        Log.i(TAG, "ScreenCast run");
        boolean isStart = startProjection(608, 1080);
        if (isStart) {
            mHandler.sendEmptyMessage(ScreenCast.MSG_ON_START_CALLBACK);
        }
    }

    private static class VirtualDisplayCallback extends VirtualDisplay.Callback {

        private WeakReference<ScreenCast> mReference;

        public VirtualDisplayCallback(ScreenCast screenCast) {
            Log.i(TAG, "VirtualDisplayCallback");
            mReference = new WeakReference<>(screenCast);
        }

        @Override
        public void onResumed() {
            if (null == mReference) {
                Log.i(TAG, "onResumed mReference is null");
                return;
            }
            ScreenCast screenCast = mReference.get();
            if (screenCast == null) {
                Log.i(TAG, "onResumed screenCast is null");
                return;
            }

            Log.i(TAG, "VirtualDisplayCallback onResumed");
            screenCast.mVideoEncoder = new VideoEncoder(screenCast.mEncoder, screenCast.mHandler, false);
            screenCast.mVideoEncoder.start();
        }

        @Override
        public void onPaused() {
            if (null == mReference) {
                Log.i(TAG, "onPaused mReference is null");
                return;
            }
            ScreenCast screenCast = mReference.get();
            if (screenCast == null) {
                Log.i(TAG, "onPaused screenCast is null");
                return;
            }
            Log.i(TAG, "VirtualDisplayCallback onPaused");
        }

        @Override
        public void onStopped() {
            Log.i(TAG, "VirtualDisplayCallback onStop");
        }

    }


    private class ListenerCallbackHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage Message:" + msg.what);
            switch (msg.what) {
                case MSG_ON_STOP_CALLBACK:
                    break;
                case MSG_ON_START_CALLBACK:
                    break;
                case MSG_ON_ERROR_CALLBACK:
                    break;
                case MSG_RESIZE_SCREEN:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private boolean startProjection(int w, int h) {
        boolean started = startEncoder(w, h);
        if (!started) {
            Log.w(TAG, "startEncoder failed");
            return false;
        }
        if (mMediaProjection == null) {
            Log.w(TAG, "mMediaProjection is null");
            return false;
        }
        try {
            mVirtualDisplayCallback = new VirtualDisplayCallback(this);
            Log.i(TAG, "startEncoder w:" + w + ",h;" + h);
            Log.i(TAG, "mMediaProjection " + mMediaProjection);
            Log.w(TAG, "DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC " + DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    w, h, 1,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, mVirtualDisplayCallback, mHandler);
            Log.d(TAG, "mSinkWidth: " + w + " mSinkHeight: " + h);
            MediaProjectionCallback callback = new MediaProjectionCallback();
            //callback should not be null
            mMediaProjection.registerCallback(callback, mHandler);
            return true;
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        return false;
    }

    private static class MediaProjectionCallback extends MediaProjection.Callback {

        @Override
        public void onStop() {
            Log.i(TAG, "MediaProjectionCallback onStop");
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private boolean startEncoder(int w, int h) {
        Log.i(TAG, "startEncoder start");
        stopEncoder();
        Log.d(TAG, "startEncoder w: " + w + " h: " + h);
        try {
            MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, w, h);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 6);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            mEncoder.configure(format, null, null,
                    MediaCodec.CONFIGURE_FLAG_ENCODE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSurface = mEncoder.createInputSurface();
            }
            Log.d(TAG, "created input surface: " + mSurface);
            mEncoder.start();
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        Log.d(TAG, "startEncoder end");
        return true;
    }

    private void stopEncoder() {
        Log.d(TAG, "stopEncoder");
        if (mEncoder != null) {
            try {
                mEncoder.stop();
                mEncoder.release();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            mEncoder = null;
        }
        Log.d(TAG, "Surface release");
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
    }

    public synchronized void release() {
        Log.i(TAG, "release");
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        stopProjection();
        if (mEncoder != null) {
            try {
                mEncoder.stop();
                mEncoder.release();
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            mEncoder = null;
        }
        if (null != mSurface) {
            mSurface.release();
            mSurface = null;
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (null != mVirtualDisplayCallback) {
            mVirtualDisplayCallback = null;
        }
    }

    private void stopProjection() {
        Log.d(TAG, "stopProjection");
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            Log.d(TAG, "mMediaProjection.stop");
        }
    }
}
