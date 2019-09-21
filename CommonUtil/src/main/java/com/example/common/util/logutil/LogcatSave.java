package com.example.common.util.logutil;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
* 保存sd开需要权限WRITE_EXTERNAL_STORAGE READ_EXTERNAL_STORAGE
* 如果是android6及以上系统，还需要动态申请权限
* 当然也可以保存在本应用的目录下，不需要权限
*
*/

/**
 * Created by hzz
 * Used log日志统计保存到本地文件
 * 参考资料 Android将应用log信息保存文件：http://www.cnblogs.com/weixing/p/3414164.html
 */
public class LogcatSave {
    private static LogcatSave INSTANCE = null;
    /**
     * 日志文件保存路径
     */
    private static String PATH_LOGCAT = "/sdcard/logcat";
    private LogDumper mLogDumper = null;
    /**
     * 应用进程ID
     */
    private int mPId;
    private final static String TAG = "LogcatSave";

    /**
     * 初始化目录 "包名+logcat"
     */
    public void init(Context context) {
        if (PATH_LOGCAT == null) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
                PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + context.getPackageName() + File.separator + "logcat";
            } else {
                //如果SD卡不存在，就保存到本应用的目录下【why 直接保存到本应用的目录下】
                PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + "logcat";
            }
        }
        Log.i(TAG, "init PATH_LOGCAT:" + PATH_LOGCAT);
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            //创建log保存目录
            boolean isSuccess = file.mkdirs();
        }
    }

    /**
     * 单例模式
     */
    public static LogcatSave getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatSave(context);
        }
        return INSTANCE;
    }

    private LogcatSave(Context context) {
        init(context);
        mPId = android.os.Process.myPid();
    }

    public void start() {
        if (mLogDumper == null)
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
        mLogDumper.start();
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        private String mPID;
        private FileOutputStream out = null;

        public LogDumper(String pid, String dir) {
            mPID = pid;
            try {
                out = new FileOutputStream(new File(dir, "logcat-" + getFileName() + ".log"));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            /**
             *
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
             *
             * 显示当前mPID程序的 E和W等级的日志.
             *
             * */

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
            //cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";
            Log.i(TAG,"cmds:"+cmds);

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((getDateEN() + "  " + line + "\n")
                                .getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

    //todo
    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        String date = format.format(new Date(System.currentTimeMillis()));
        Log.i(TAG,"getFileName date:"+date);
        return date;// 2012-10-03
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }
}