package com.github.tvbox.osc.util;

import android.content.Context;
import android.os.Looper;

import com.github.tvbox.osc.data.CustomData;
import com.orhanobut.hawk.Hawk;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.LinkedHashMap;
import java.util.Map;

public class CrashManagerUtil {

    private static CrashManagerUtil mInstance;
    private static Context mContext;

    private CrashManagerUtil() {

    }

    public static CrashManagerUtil getInstance(Context context) {
        if (mInstance == null) {
            mContext = context.getApplicationContext();
            mInstance = new CrashManagerUtil();
        }
        return mInstance;
    }

    public void init() {
        // bugly
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(mContext);
        strategy.setCrashHandleCallback(new CrashReport.CrashHandleCallback() {
            public Map<String, String> onCrashHandleStart(int crashType, String errorType,
                                                          String errorMessage, String errorStack) {
                LOG.d("上报异常日志Map");
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("AppModelType", CustomData.getInstance().GetCurrAppModelType().toString());
                map.put("AppModelTypeName", CustomData.getInstance().GetCurrAppModelTypeName());
                map.put("API_URL", Hawk.get(HawkConfig.API_URL));
                map.put("HOME_API", Hawk.get(HawkConfig.HOME_API));
                return map;
            }

            @Override
            public byte[] onCrashHandleStart2GetExtraDatas(int crashType, String errorType,
                                                           String errorMessage, String errorStack) {
                try {
                    LOG.d("上报异常日志");
                    return LOG.ReadLogFile();
                    // return "Extra data.".getBytes("UTF-8");
                } catch (Exception e) {
                    LOG.e("上报异常日志出现异常", e);
                    return null;
                }
            }

        });
        CrashReport.initCrashReport(mContext, "962ef26c61", true, strategy);
        CrashReport.setAllThreadStackEnable(mContext, true, true); // 打开全部线程

        //crach 防护  开启后（优点）卡死不会闪退，界面还在（缺点）不会上报上面的自定义数据和日志
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                LOG.e("crach 防护 uncaughtException-->" , e.toString());
//                CrashReport.postCatchedException(e, t);
//                handleFileException(e);
//                if (t == Looper.getMainLooper().getThread()) {
//                    handleMainThread(e);
//                }
//            }
//        });
    }

    //这里对异常信息作处理，可本地保存，可上传至第三方平台
    private void handleFileException(Throwable e) {
        LOG.e("CrashManagerUtil监听到异常错误", e.toString());
        CrashReport.postCatchedException(e);
    }

    private void handleMainThread(Throwable e) {
        while (true) {
            try {
                Looper.loop();
            } catch (Throwable e1) {
                handleFileException(e1);
            }
        }
    }
}