package com.example.commonutil;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.example.commonutil.mirror.ScreenCastService;

public class MirrorPermissionFragment extends Fragment {
    public static final String TAG = "MirrorPermission";

    private Context mContext;
    private static final int REQUEST_MIRROR_PERMISSION = 1;
    private IMediaProjectionListener mMediaProjectionListener = null;
    private MediaProjection mMediaProjection;
    private MediaProjectionManager mProjectionManager;
    private ScreenServiceConn serviceConn;
    public interface IMediaProjectionListener {
        void onResult(MediaProjection mediaProjection);
    }

    public static MirrorPermissionFragment newInstance(){
        MirrorPermissionFragment fragment = new MirrorPermissionFragment();
        return fragment;
    }
    public void setMediaProjectionListener(IMediaProjectionListener mediaProjectionListener) {
        mMediaProjectionListener = mediaProjectionListener;
    }

    public void setAppContext(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        setRetainInstance(true);
        registerMediaProjectionPermission();
    }

    public void registerMediaProjectionPermission() {
        try {
            if (mProjectionManager == null) {
                mProjectionManager = (MediaProjectionManager) getActivity()
                        .getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
        try {
            Intent intent = mProjectionManager.createScreenCaptureIntent();
            startActivityForResult(intent, REQUEST_MIRROR_PERMISSION);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == REQUEST_MIRROR_PERMISSION) {
                Log.i(TAG, "onActivityResult mMediaProjectionListener:" + mMediaProjectionListener);
                if (mMediaProjectionListener != null) {
                    mMediaProjectionListener.onResult(mProjectionManager.getMediaProjection(Activity.RESULT_OK,
                            data));
                } else {
                    // 有权限了
                    startMirror(data);
                }
            } else {

            }
        } else {
            if (requestCode == REQUEST_MIRROR_PERMISSION) {

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startMirror(Intent data){
        Log.i(TAG, "startMirror context:" + mContext);
        if (null != mContext) {
            mMediaProjection = mProjectionManager.getMediaProjection(Activity.RESULT_OK,
                    data);
            Intent intent = new Intent(mContext, ScreenCastService.class);
            intent.putExtra(ScreenCastService.KEY_MIRROR_SWTICH, ScreenCastService.STATE_INIT);
//            intent.putExtra(ScreenCastService.KEY_BROWSER_INFO, mBrowserInfo);
//            intent.putExtra(Constant.KEY_SESSION_ID, mSessionId);
//            intent.putExtra(MirrorManagerImpl.KEY_URI, mUri);
//            intent.putExtra(MirrorManagerImpl.KEY_RUSOLUTION_HEIGHT, mHeight);
//            intent.putExtra(MirrorManagerImpl.KEY_RUSOLUTION_WIDTH, mWidth);
//            intent.putExtra(MirrorManagerImpl.KEY_BITRATE, mBitRate);
//            intent.putExtra(MirrorManagerImpl.KEY_SCREEN_CODE, mScreenCode);
//            intent.putExtra(MirrorManagerImpl.KEY_AUDIOMIRROR_ONOFF, isAudioOpen);
//            intent.putExtra(MirrorManagerImpl.KEY_FULLSCREEN, isFullScreen);
            mContext.startService(intent);
            serviceConn = new ScreenServiceConn(mMediaProjection);
            mContext.bindService(intent, serviceConn, Context.BIND_AUTO_CREATE);
        }
    }

    public void stopMirror() {
        if (null != mContext) {
            try {
                Intent intent = new Intent(mContext, ScreenCastService.class);
                intent.putExtra(ScreenCastService.KEY_MIRROR_SWTICH, ScreenCastService.STATE_OFF);
                mContext.startService(intent);
                mContext.unbindService(serviceConn);
                serviceConn = null;
            } catch (Exception e) {
                Log.w(TAG, e);
            }

        }

    }


    private static class ScreenServiceConn implements ServiceConnection {

        private MediaProjection mMediaProjection;
        private ScreenCastService castService;

        public ScreenServiceConn(MediaProjection mediaProjection) {
            mMediaProjection = mediaProjection;
        }
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "ScreenServiceConn onServiceConnected");
            castService = ((ScreenCastService.ScreenBinder) service).getService();
            if (null != castService) {
                castService.setMediaProjection(mMediaProjection);
//                castService.setPlayerListener(mPlayerListener);
                castService.startMirror();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "ScreenServiceConn onServiceDisconnected");
        }
    }
}
