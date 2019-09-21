package com.example.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetWorkUtil {
    public static final String TAG = "NetWorkUtil";

    public static final String ETHERNET = "Ethernet";
    public static final String WIFI = "WIFI";
    public static final String MOBILE = "MOBILE";

    public static final int ETH0 = 1;
    public static final int WLAN0 = 2;

    //获取ip最后几位
    public static String getLastIpPart(Context context,String ip) {
        String lastStr = ip;
        String[] dump = ip.split("\\.");
        if (dump.length > 0) {
            lastStr = dump[dump.length - 1];
        }
        Logger.i(TAG, "createDeviceName: " + lastStr);
        return lastStr;
    }



    public static boolean isNetConnect(Context context,String netTypeName) {
        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase(netTypeName)) {
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public static String getIP(Context context,int type) {
        String ip = "";
        try {
            // 在有些设备上wifi和有线同时存在，获得的ip会有两个
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            if (en == null) {
                return ip;
            }
            while (en.hasMoreElements()) {
                NetworkInterface element = en.nextElement();
                Enumeration<InetAddress> inetAddresses = element.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        ip = inetAddress.getHostAddress().toString();
                        Logger.i(TAG, "type: " + type);
                        Logger.i(TAG, "getIPAddress: " + ip);
                        if (type == 1) {
                            if (element.getDisplayName().equals("eth0")) {
                                return ip;
                            }
                        } else if (type == 2) {
                            if (element.getDisplayName().equals("wlan0")) {
                                return ip;
                            }
                        } else {
                            return "";
                        }
                        break;
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.w(TAG, ex);
        }
        return "";
    }

    /* 获取WiFi的SSID */
    /* 判断当前网路有线还是WiFi */
    public static String getNetWorkName(Context context) {

        String wired_network = "有线网络";
        String mobile_network = "移动网络";
        String net_error = "网络错误";

        try {
            ConnectivityManager manager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                String type = networkInfo.getTypeName();
                if (type.equalsIgnoreCase("Ethernet")) {
                    return wired_network;
                } else if (type.equalsIgnoreCase("WIFI")) {
                    String tmpssid = getWifiSSID(context);

                    if (tmpssid.contains("unknown") || tmpssid.contains("0x")) {
                        tmpssid = wired_network;
                    }
                    return tmpssid;
                } else if (type.equalsIgnoreCase("MOBILE")) {
                    return mobile_network;
                } else {
                    return wired_network;
                }
            } else {
                String apName = getAPName(context);
                if (!TextUtils.isEmpty(apName)) {
                    return apName;
                }
                return net_error;
            }
        } catch (Exception e) {
            Logger.w(TAG, e);
            return net_error;
        }
    }

    private static String getAPName(Context context) {
        if (!isWifiApOpen(context)) {
            return "";
        }
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(manager);
            return configuration.SSID;
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
        return "";
    }

    public static boolean isWifiApOpen(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getDeclaredMethod("getWifiApState");
            int state = (int) method.invoke(manager);
            Field field = manager.getClass().getDeclaredField("WIFI_AP_STATE_ENABLED");
            int value = (int) field.get(manager);
            if (state == value) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
        return false;
    }

    private static String getWifiSSID(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (TextUtils.isEmpty(wifiInfo.getSSID())) {
            return null;
        }

        String wifiName = wifiInfo.getSSID();
        if (wifiName.contains("\"")) {
            return wifiName.replace("\"", "");
        }

        return wifiName;
    }
}
