package com.example.commonutil;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.common.util.Logger;
import com.example.common.util.NetWorkUtil;
import com.example.common.util.logutil.LogcatSave;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        checkPermission();
//        Logger.i(TAG, "onCreate test logcat save");
        String s1 = NetWorkUtil.getIP(this, 1);
        String s2 = NetWorkUtil.getIP(this, 2);
        Logger.i(TAG, "s1:" + s1 + ",s2:" + s2);
        String s3 = NetWorkUtil.getLastIpPart(this, s2);
        Logger.i(TAG, "s3:" + s3);
        Logger.i(TAG, "onCreate getNetWorkName:" + NetWorkUtil.getNetWorkName(this));
        Logger.i(TAG, "onCreate isWifiApOpen:" + NetWorkUtil.isWifiApOpen(this));
        Logger.i(TAG, "onCreate " + this.getApplication());
        Logger.i(TAG, "onCreate " + getCurrentApplication());
    }

    private void checkPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] permissionArr = new String[]{};
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(permissionArr), 100);
            return;
        }
        // test logSave
        LogcatSave.getInstance(this).start();
    }

    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 100) {
            Log.w(TAG, "onRequestPermissionsResult failed requestCode: " + requestCode);
            finish();
            return;
        }
        if (grantResults.length <= 0) {
            Log.w(TAG, "onRequestPermissionsResult grantResults.length: " + grantResults.length);
            finish();
            return;
        }
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "onRequestPermissionsResult grantResults[0]: " + grantResults[0]);
            finish();
            return;
        }
        // test logSave
        LogcatSave.getInstance(this).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // test logSave
        LogcatSave.getInstance(this).stop();
    }


    //get current Application context
    private Application getCurrentApplication() {
        Application CURRENT;
        Object activityThread = null;
        try {
            Method method = Class.forName("android.app.ActivityThread")
                    .getMethod("currentActivityThread");
            method.setAccessible(true);
            activityThread = method.invoke(null);
            Object app = activityThread.getClass().getMethod("getApplication")
                    .invoke(activityThread);
            CURRENT = (Application) app;
        } catch (Throwable e) {
            throw new IllegalStateException("Can not access Application context by magic code, boom!", e);
        }
        return CURRENT;
    }

}
