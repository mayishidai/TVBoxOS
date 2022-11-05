package com.github.tvbox.osc.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler.Callback;
import android.os.Message;

public class ToolUtils {
    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
    /*
    * 主线程运行方法
    */
    public static void runOnUiThread(Activity activity, Callback callback, Message message){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callback.handleMessage(message);
            }
        });
    }
}
