package com.github.tvbox.osc.util;

import android.content.Context;
import android.util.Log;

import com.github.tvbox.osc.base.App;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class LOG {
    private static String TAG = "TVBox";
    private static boolean isSaveLog = false;
    private static int saveDay = 2;
    private static File file;
    private static File logcatFile;

    public static void i(Object... msgs){
        LogPrint(Log.INFO, FormatMsg(msgs));
    }
    public static void d(Object... msgs){
        LogPrint(Log.DEBUG, FormatMsg(msgs));
    }
    public static void w(Object... msgs){
        LogPrint(Log.WARN, FormatMsg(msgs));
    }
    public static void e(Object... msgs){
        LogPrint(Log.ERROR, FormatMsg(msgs));
    }
    public static void printStackTrace(Exception ex, Object... msgs){
        e(ex);
        e(msgs);
        ex.printStackTrace();
        CrashReport.postCatchedException(ex);
    }
    public static void printStackTrace(Throwable th, Object... msgs){
        e(th);
        e(msgs);
        th.printStackTrace();
        CrashReport.postCatchedException(th);
    }
    public static void OpenSaveLog(){
        LOG.i("LOG", "打开日志存储系统");
        try {
            Context context = App.getInstance().getBaseContext();
            isSaveLog = true;
            if (file == null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH"/*mmss"*/);
                Date now = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
                File logDir = new File(context.getExternalFilesDir("logs").getAbsolutePath());
                if (!logDir.exists())
                    logDir.mkdirs();
                else{ // 删除超时日志文件
                    File[] tempList = logDir.listFiles();
                    for (int i = 0; i < tempList.length; i++) {
                        if (tempList[i].isFile()) {
                            String fileName = tempList[i].getName();
                            if (fileName.endsWith(".log") || fileName.endsWith(".logcat")){    //  根据自己的需要进行类型筛选
                                try {
                                    //文件时间减去当前时间
                                    Date start = dateFormat.parse(dateFormat.format(new Date(tempList[i].lastModified())));
                                    long diff = now.getTime() - start.getTime();//这样得到的差值是微秒级别
                                    long days = diff / (1000 * 60 * 60 * 24);
                                    if(saveDay <= days){
                                        tempList[i].delete();
                                    }
                                } catch (Exception e){
                                    e( "OpenSaveLog", "dataformat exeption e " + e.toString());
                                }
                            }
                        }
                    }
                }
                file = new File(logDir, dateFormat.format(now)+".log");
                if (!file.exists())
                    file.createNewFile();
                d("普通日志文件存储位置：", file.getAbsolutePath());

                logcatFile = new File(logDir, dateFormat.format(now)+".logcat");
                if (!logcatFile.exists())
                    logcatFile.createNewFile();
                OpenLogcat();
                d("Logcat日志文件存储位置：", logcatFile.getAbsolutePath());
            }
        }catch (Exception e){
            e.printStackTrace();
            isSaveLog = false;
            file = null;
            logcatFile = null;
        }
    }
    public static void ClsoeSaveLog(){
        LOG.i("LOG", "关闭日志存储系统");
        isSaveLog = false;
        if (file!=null)
            file = null;
    }
    private static String FormatMsg(Object... msgs){
        String msgStr = "";
        for (Object msg : msgs){
            msgStr += String.format("%s    ", msg);
        }
        return msgStr;
    }
    private static void LogPrint(int logType, String msg){
        if (logType == Log.ERROR)
            Log.e(TAG, msg);
        else if (logType == Log.WARN)
            Log.w(TAG, msg);
        else if (logType == Log.DEBUG)
            Log.d(TAG, msg);
        else if (logType == Log.INFO)
            Log.i(TAG, msg);
        else
            Log.e(TAG, msg);
        WriteFile(logType, msg);
    }
    private static void WriteFile(int logType, String msg){
        if (isSaveLog && file!=null)
            FileUtils.appendFile(file, String.format("%s   %s\n", logType, msg));
    }
    public static byte[] ReadLogFile(){
        if (isSaveLog && file!=null)
            return FileUtils.readSimple(file);
        return null;
    }

    public static void OpenLogcat(){
        //第一个是Logcat ，也就是我们想要获取的log日志
        //第二个是 -s 也就是表示过滤的意思
        //第三个就是 我们要过滤的类型 W表示warm ，我们也可以换成 D ：debug， I：info，E：error等等
        String[] running = new String[]{"logcat","-s","adb logcat *: D"};
        try {
            Process exec = Runtime.getRuntime().exec(running);
            final InputStream inputS = exec.getInputStream();
            new Thread() {
                @Override
                public void run() {
                    FileOutputStream os = null;
                    try {
                        Log.e(TAG,"开始在线写入logcat, 目前有问题，记录不下来");
                        //新建一个路径信息
                        os = new FileOutputStream(logcatFile);
                        int len = 0;
                        byte[] buf = new byte[1024];
                        while (-1 != (len = inputS.read(buf))) {
                            os.write(buf, 0, len);
                            os.flush();
                        }
                    } catch (Exception e) {
                        printStackTrace(e, "读取Logcat线程报错");
                    } finally {
                        if (null != os) {
                            try {
                                os.close();
                                os = null;
                            } catch (IOException e) {
                                // Do nothing
                            }
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            printStackTrace(e, "打开Logcat线程报错");
        }
    }
}