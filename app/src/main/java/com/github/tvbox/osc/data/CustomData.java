package com.github.tvbox.osc.data;

import android.os.Bundle;
import android.util.Log;

import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.RemoteConfig;
import com.github.tvbox.osc.util.RemoteConfigName;
import com.google.gson.JsonElement;
import com.orhanobut.hawk.Hawk;

public class CustomData {
    private static CustomData instance;
    public static CustomData getInstance(){
        if (instance==null)
            instance = new CustomData();
        return instance;
    }

    // region APP模式类型
    public static final String APP_MODEL_TYPE = "APP_MODEL_TYPE"; // APP模式
    public enum AppModelType {
        YOUND, // 默认/青年版
        AGED // 老年版
    }
    public void SetAppModelType(AppModelType appModelType){
        Hawk.put(APP_MODEL_TYPE, appModelType);
        if (AppManager.getInstance().isActivity()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean("useCache", true);
            BaseActivity currActivity = (BaseActivity) AppManager.getInstance().currentActivity();
            currActivity.jumpActivity(HomeActivity.class, bundle);
        }
    }
    public AppModelType GetCurrAppModelType(){
        return Hawk.get(APP_MODEL_TYPE, AppModelType.YOUND);
    }
    public String GetCurrAppModelTypeJsonName(){
        if (GetCurrAppModelType()==AppModelType.YOUND){
            return RemoteConfigName.CustomData_YOUND;
        }else if (GetCurrAppModelType()==AppModelType.AGED) {
            return RemoteConfigName.CustomData_AGED;
        }
        return RemoteConfigName.CustomData_YOUND;
    }
    public String GetCurrAppModelTypeName() {
        return GetCurrAppModelTypeName(GetCurrAppModelType());
    }
    public String GetCurrAppModelTypeName(AppModelType appModelType){
        String showText = "未定义类型";
        switch (appModelType)
        {
            case AGED:
                showText = "老年版";
                break;
            case YOUND:
                showText = "青年版";
                break;
        }
        return showText;
    }
    // endregion

    // region 首页按钮控制
    public boolean GetHomeButtonVisition(String buttonName){
        JsonElement element = RemoteConfig.GetValue(
                RemoteConfigName.CustomData,
                GetCurrAppModelTypeJsonName(),
                RemoteConfigName.APPModel_HomeButtons,
                buttonName);
        return element.isJsonNull()||element.getAsBoolean();
    }
    // endregion

    // region TTs
    public String GetTTSDownloadUrl(){
        JsonElement element = RemoteConfig.GetValue(RemoteConfigName.TTSDownLoad);
        Log.e(this.getClass().getName(), "GetTTSDownloadUrl: "+element);
        return (element==null || element.isJsonNull()) ?  "http://76.mayishidai.cn/tv/api/tts/%E7%A7%91%E5%A4%A7%E8%AE%AF%E9%A3%9E%E8%AF%AD%E9%9F%B3%E5%BC%95%E6%93%8E3.0.apk" : element.getAsString();
    }
    // endregion
}