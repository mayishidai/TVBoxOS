package com.github.tvbox.osc.data;

import com.github.tvbox.osc.util.HawkConfig;
import com.orhanobut.hawk.Hawk;

public class CustomData {
    public enum AppModelType {
        YOUND, // 默认/青年版
        AGED // 老年版
    }

    private static CustomData instance;
    public static CustomData getInstance(){
        if (instance==null)
            instance = new CustomData();
        return instance;
    }

    // APP模式
    public void SetAppModelType(AppModelType appModelType){
        Hawk.put(HawkConfig.APP_MODEL_TYPE, appModelType);
    }
    public AppModelType GetAppModelType(){
        return Hawk.get(HawkConfig.APP_MODEL_TYPE, AppModelType.YOUND);
    }
    public String GetAppModelTypeName() {
        return GetAppModelTypeName(GetAppModelType());
    }
    public String GetAppModelTypeName(AppModelType appModelType){
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
}
