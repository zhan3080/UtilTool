package com.example.common.util.wifiutil;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.common.util.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * author : zhan3080
 * date   : 12/15/21 21:40 PM
 * desc   : wifi工具类
 */
public class WifiUtil {
    private static final String TAG = "WifiUtil";


    public WifiUtil(Context context){
        Log.i(TAG,"UsbMux ");
    }

    // 通过ssid获取密码（需要系统权限android.uid.system）
    public static String getPasswordBySSID(Context context,String ssid){
        Logger.i(TAG,"getPasswordBySSID " + ssid);
        WifiManager manager = null;
        String pwd = "";
        try {
            manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getMethod("getPrivilegedConfiguredNetworks");
            method.setAccessible(true);
            List<WifiConfiguration> mList = (List<WifiConfiguration>) method.invoke(manager);
            for (WifiConfiguration object : mList) {
                String shareSSID = object.SSID;
                String sharedKey = ((WifiConfiguration)object).preSharedKey;
                Logger.i(TAG,"getPasswordBySSID shareSSID" + shareSSID + ", sharedKey:" + sharedKey);
                if(shareSSID.equals(ssid)){
                    pwd = sharedKey;
                }
            }
        }catch (Exception e){
            Logger.w(TAG,"getPasswordBySSID " + e);
        }

        return pwd;
    }


    // 需要系统权限android.uid.system
    public static WifiConfiguration getWifiConfiguration(Context context){
        WifiConfiguration mWifiConfig = null;
        try{
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = wifiManager.getClass().getMethod("getWifiConfiguration");
            method.setAccessible(true);
            mWifiConfig = (WifiConfiguration) method.invoke(wifiManager);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return mWifiConfig;
    }

    // 获取热点的密码（需要系统权限android.uid.system）
    public static String getApSSIDAndPwd(Context context, int flag) {
        WifiConfiguration mWifiConfig = getWifiConfiguration(context);
        String ssid = null;
        String pwd = null;
        if (null != mWifiConfig) {
            Field[] fields = mWifiConfig.getClass().getDeclaredFields();
            if (null != fields) {
                for (Field field : fields){
                    try {
                        if (field.getName().equals("SSID")) {
                            ssid = field.get(mWifiConfig).toString();
                            Logger.e(TAG, "AP SSI = " + ssid);
                        } else if (field.getName().equals("preSharedKey")) {
                            pwd = field.get(mWifiConfig).toString();
                            Logger.e(TAG, "AP pwd = " + pwd);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        Logger.e(TAG, "getApSSIDAndPwd()-->error:" + e);
                    }
                }
            }
        }
        if (null == ssid){
            ssid = "Unknown";
        }
        if (null == pwd) {
            pwd = "Unknown";
        }
        if (0 == flag){
            return ssid;
        }else {
            return pwd;
        }
    }
}


