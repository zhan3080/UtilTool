package com.example.commonutil;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class UsbFile {
    private static final String TAG = "UsbFile";
    private Context mContext;
//    private UsbManager mUsbManager;

    public UsbFile(Context context) {
        mContext = context;
//        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
    }

    public String getFilePath(){
        String s = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.i(TAG,"getFilePath : " + s);
        File file = new File(s);
        for(File f:file.listFiles()){
            Log.i(TAG,"File path " + f.getAbsolutePath());
        }
        return s;
    }

    public String getPublicPath(){
//        String s = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();
        String s = "/mnt/media_rw";
        Log.i(TAG,"getPublicPath : " + s);
        File file = new File(s);
        if(file == null){
            return "";
        }
        for(File f:file.listFiles()){
            if(f == null){
                return "";
            }
            Log.i(TAG,"File path " + f.getAbsolutePath());
        }

//        File[] files = Environment.getExternalStoragePublicDirectory(Environment.MEDIA_MOUNTED);
//        for(File file:files){
//            Log.e("getPublicPath ",file.getAbsolutePath());
//        }

        return s;
    }
}
