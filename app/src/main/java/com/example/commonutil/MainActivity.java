package com.example.commonutil;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.common.util.Logger;
import com.example.common.util.XMLParser;
import com.example.common.util.logutil.LogcatSave;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);
//        checkPermission();
//        Logger.i(TAG, "onCreate test logcat save");
//        String s1 = NetWorkUtil.getIP(this, 1);
//        String s2 = NetWorkUtil.getIP(this, 2);
//        Logger.i(TAG, "s1:" + s1 + ",s2:" + s2);
//        String s3 = NetWorkUtil.getLastIpPart(this, s2);
//        Logger.i(TAG, "s3:" + s3);
//        Logger.i(TAG, "onCreate getNetWorkName:" + NetWorkUtil.getNetWorkName(this));
//        Logger.i(TAG, "onCreate isWifiApOpen:" + NetWorkUtil.isWifiApOpen(this));

//        Logger.i(TAG, "onCreate " + this.getApplication());
//        Logger.i(TAG, "onCreate " + getCurrentApplication());

//        Logger.i(TAG, "onCreate wifi Direct:" + getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT));
//        Logger.i(TAG, "TEST MD5:" + StringByteUtil.getMd5("test"));

        parseXMLTest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
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

    //test xml parse
    private void parseXMLTest() {
        Log.i(TAG, "parseXMLTest");
        InputStream inputStream = null;
        try {
            inputStream = this.getApplicationContext().getClass().getResourceAsStream("/assets/" + "config.xml");
        } catch (Exception e) {
            Log.w(TAG, e);
            inputStream = null;
        }
        if(inputStream == null){
            AssetManager am = this.getAssets();
            try {
                inputStream = am.open("config.xml");
            } catch (Exception e) {
                Log.w(TAG, e);
                inputStream = null;
            }
        }
        Log.i(TAG, "parseXMLTest inputStream:" + inputStream);
        if (inputStream != null) {
            XMLParser xmlParser = new XMLParser();
            xmlParser.parser(inputStream,"DefaultModel");
            xmlParser.getXMLString();
        }
    }

}
