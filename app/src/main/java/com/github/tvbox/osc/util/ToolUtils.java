package com.github.tvbox.osc.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler.Callback;
import android.os.Message;

import androidx.core.content.FileProvider;

import java.io.File;

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

    //下载成功后自动安装apk并打开
    public static void installApk(Context context, File file){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            LOG.e(context.getApplicationInfo().processName, file.getAbsolutePath());
            Uri uri = FileProvider.getUriForFile(context, context.getApplicationInfo().processName+".fileprovider", file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        }
        LOG.i("打开 下载文件", Uri.fromFile(file).getPath());
        try {
            context.startActivity(intent);
        }catch (Exception e){
            LOG.e("更新下载安装出现异常",e.toString());
        }
    }
}
