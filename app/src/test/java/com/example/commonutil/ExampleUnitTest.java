package com.example.commonutil;

import org.junit.Test;

import java.security.MessageDigest;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    public static void main(String[] args){
        createDeviceId("00:95:11:A0:27:E7","com.hpplay.happyplay.dongle");
    }

    public static String createDeviceId(String mac, String packageName) {
        String deviceId = mac;
        String macString = mac.replace(":", "");
        byte[] macbytes = macString.getBytes();
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(packageName.getBytes());
            String result = "";
            //去除:的mac地址长度是12
            for (int index = 0; index < 12; index++) {
                //mac地址与包名md5的前12位分别相加后，按16进制取值
                String temp = Integer.toHexString((macbytes[index] + bytes[index]) & 0x0f);
                result += temp;
            }
            //12位的字符串再转成mac地址形式
            String resultMac = result.replaceAll ("(.{2})", "$1:");//间隔两位加入：分隔
            deviceId = resultMac.substring(0,resultMac.length()-1);

            System.out.println(":" + deviceId);
        } catch (Exception e) {
            System.out.println("test "+e);
        }
        System.out.println("createDeviceId deviceId:" + deviceId);
        return deviceId;
    }
}