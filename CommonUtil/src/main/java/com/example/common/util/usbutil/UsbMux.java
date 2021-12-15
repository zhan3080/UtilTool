package com.xxx.hzz.mediacodecapplication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * author : zhan3080
 * date   : 12/15/21 21:40 PM
 * desc   : usb监听检测识别
 */
public class UsbMux {
    private static final String TAG = "UsbMux";
    private static final String ACTION_USB_PERMISSION = "test.usb.permission";
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;


    public UsbMux(Context context){
        Log.i(TAG,"UsbMux ");
        mUsbManager = (UsbManager)context.getSystemService(context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(context,0
                ,new Intent(ACTION_USB_PERMISSION),0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED); // usb插入广播事件
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED); // usb拔出广播事件
        context.registerReceiver(receiver,filter);

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                discoverDevice();
//            }
//        },2000,1000);
    }


    private void discoverDevice(){
        if(mUsbManager == null){
            return;
        }

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        if(deviceList == null || deviceList.isEmpty()){
            return;
        }
        for(Map.Entry<String, UsbDevice> entry : deviceList.entrySet()){
            Log.i(TAG,"discoverDevice key:" + entry.getKey());
            UsbDevice device = entry.getValue();
            printDevices(device);
            if (mUsbManager.hasPermission(device)) {
                Log.i(TAG, "discoverDevice hasPermission");
//                registDevice(device);
            } else {
                Log.i(TAG, "discoverDevice requestPermission " + device);
                mUsbManager.requestPermission(device, mPermissionIntent);
            }
        }
    }

    private void registDevice(UsbDevice device) {
        if (mUsbManager == null) {
            Log.w(TAG, "registDevice ignore");
            return;
        }
        Log.i(TAG, "registDevice name:" + device.getDeviceName());
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        Log.i(TAG,"receiver connection:" + connection);
    }

    private void printDevices(UsbDevice device) {
        StringBuilder builder = new StringBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.append("ManufacturerName:");
            builder.append(device.getManufacturerName());
            builder.append("  ");
            builder.append("ProductName:");
            builder.append(device.getProductName());
            builder.append("  ");
        }
        builder.append("ProductId:");
        builder.append(device.getProductId());
        builder.append("  ");
        builder.append("VendorId:");
        builder.append(device.getVendorId());
        Log.i(TAG, "UsbDevice:" + builder.toString());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"receiver action:" + intent.getAction());
            if(intent.getAction().equals(ACTION_USB_PERMISSION)){
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean status = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false);
                Log.i(TAG,"receiver status:" + status);
                Log.i(TAG, "receiver get permission for device " + device);
                if(status){
                    Log.i(TAG, "receiver get permission for deviceName " + device.getDeviceName());
                    printDevices(device);
                    registDevice(device);
                }
            }
            if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                discoverDevice();
            }
        }
    };
}


