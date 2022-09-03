package com.github.tvbox.osc.util;

import android.content.Intent;
import android.os.Bundle;

import com.github.tvbox.osc.base.App;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.ui.dialog.UpdateDialog;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;


/*
 * 远程文件配置
 */
public class RemoteConfig {
    private static String remoteUrl = "http://43.128.88.114/tv/apk/remote.ini";
    private static JsonObject remoteJsonObject;
    private static boolean isRemoteConfigOk;

    public static void Init(){
        isRemoteConfigOk = false;
        OkGo.<String>get(remoteUrl).execute(new AbsCallback<String>() {
            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                return response.body().string();
            }
            @Override
            public void onSuccess(Response<String> response) {
                isRemoteConfigOk = true;
                InitRemoteConfig(response.body());
            }
        });
    }
    public static boolean IsOk(){
        return isRemoteConfigOk;
    }
    public static JsonElement GetValue(String key){
        if (IsOk() && remoteJsonObject!=null)
        {
            return remoteJsonObject.get(key);
        }
        return new JsonElement() {
            @Override
            public JsonElement deepCopy() {
                return null;
            }
        };
    }

    private static void InitRemoteConfig(String config){
        LOG.i("RemoteConfig", "获取到远端内容", config);
        remoteJsonObject = JsonParser.parseString(config).getAsJsonObject();

        // region 日志
        if (GetValue(RemoteConfigName.IsRecodeLog)!=null && GetValue(RemoteConfigName.IsRecodeLog).getAsBoolean()) {
            LOG.i("RemoteConfig", "远端打开日志保存");
            LOG.OpenSaveLog();
        }
        else {
            LOG.i("RemoteConfig", "远端关闭日志保存");
            LOG.ClsoeSaveLog();
        }
        // endregion 日志

        // region 默认配置
        // region 默认API地址
        if (GetValue(RemoteConfigName.APIUrl)!=null && !GetValue(RemoteConfigName.APIUrl).getAsString().isEmpty()) {
            String remoteValue = GetValue(RemoteConfigName.APIUrl).getAsString();
            if(SetRemoteHawkConfig(HawkConfig.API_URL, remoteValue,"默认首页API")){
                Bundle bundle = new Bundle();
                bundle.putBoolean("useCache", true);
                BaseActivity currActivity = (BaseActivity)AppManager.getInstance().currentActivity();
                currActivity.jumpActivity(HomeActivity.class, bundle);
            }
        }
        // endregion 默认API地址
        // region 默认首页数据源
        if (GetValue(RemoteConfigName.HomeID)!=null && !GetValue(RemoteConfigName.HomeID).getAsString().isEmpty()) {
            String remoteValue =  GetValue(RemoteConfigName.HomeID).getAsString();
            SetRemoteHawkConfig(HawkConfig.HOME_API, remoteValue,"默认首页数据源");
        }
        // endregion 默认首页数据源
        // region 默认首页推荐
        if (GetValue(RemoteConfigName.HomeShowType)!=null && !GetValue(RemoteConfigName.HomeShowType).getAsString().isEmpty()) {
            int remoteValue =  GetValue(RemoteConfigName.HomeShowType).getAsInt();
            SetRemoteHawkConfig(HawkConfig.HOME_REC, remoteValue,"默认首页推荐");
        }
        // endregion 默认首页推荐
        // region 默认搜索展示
        if (GetValue(RemoteConfigName.HomeSearchType)!=null && !GetValue(RemoteConfigName.HomeSearchType).getAsString().isEmpty()) {
            int remoteValue =  GetValue(RemoteConfigName.HomeSearchType).getAsInt();
            SetRemoteHawkConfig(HawkConfig.SEARCH_VIEW, remoteValue,"默认搜索展示");
        }
        // endregion 默认搜索展示
        // region 默认聚合模式
        if (GetValue(RemoteConfigName.HomeFastSearch)!=null && !GetValue(RemoteConfigName.HomeFastSearch).getAsString().isEmpty()) {
            boolean remoteValue =  GetValue(RemoteConfigName.HomeFastSearch).getAsBoolean();
            SetRemoteHawkConfig(HawkConfig.FAST_SEARCH_MODE, remoteValue,"默认聚合模式");
        }
        // endregion 默认聚合模式
        // region 默认安全DNS
        if (GetValue(RemoteConfigName.HomeDNSType)!=null && !GetValue(RemoteConfigName.HomeDNSType).getAsString().isEmpty()) {
            int remoteValue =  GetValue(RemoteConfigName.HomeDNSType).getAsInt();
            SetRemoteHawkConfig(HawkConfig.DOH_URL, remoteValue,"默认安全DNS");
        }
        // endregion 默认安全DNS
        // region 默认历史记录
        if (GetValue(RemoteConfigName.HomeHistoryNum)!=null && !GetValue(RemoteConfigName.HomeHistoryNum).getAsString().isEmpty()) {
            int remoteValue =  GetValue(RemoteConfigName.HomeHistoryNum).getAsInt();
            SetRemoteHawkConfig(HawkConfig.HISTORY_NUM, remoteValue,"默认历史记录");
        }
        // endregion 默认历史记录
        // region 默认画面缩放
        if (GetValue(RemoteConfigName.HomePictureZoom)!=null && !GetValue(RemoteConfigName.HomePictureZoom).getAsString().isEmpty()) {
            int remoteValue =  GetValue(RemoteConfigName.HomePictureZoom).getAsInt();
            SetRemoteHawkConfig(HawkConfig.PLAY_SCALE, remoteValue,"默认画面缩放");
        }
        // endregion 默认画面缩放
        // region 默认窗口预览
        if (GetValue(RemoteConfigName.HomeWindowPreview)!=null && !GetValue(RemoteConfigName.HomeWindowPreview).getAsString().isEmpty()) {
            boolean remoteValue =  GetValue(RemoteConfigName.HomeWindowPreview).getAsBoolean();
            SetRemoteHawkConfig(HawkConfig.SHOW_PREVIEW, remoteValue,"默认窗口预览");
        }
        // endregion 默认窗口预览

        // endregion 默认配置

        // region 默认更新地址
        if (GetValue(RemoteConfigName.UpdateData)!=null && GetValue(RemoteConfigName.UpdateData).getAsJsonObject() != null) {
            JsonObject updateData = GetValue(RemoteConfigName.UpdateData).getAsJsonObject();
            LOG.i("RemoteConfig", "★远端设置", "默认更新数据", updateData.toString());
            if (GetValue(RemoteConfigName.IsForceUpdate)!=null && GetValue(RemoteConfigName.IsForceUpdate).getAsBoolean()){
                LOG.i("RemoteConfig", "★远端设置", "启动强制显示更新");
                UpdateDialog.checkUpdate(AppManager.getInstance().currentActivity(), false);
            }else {
                LOG.i("RemoteConfig", "★远端设置", "启动非强制显示更新");
            }
        }
        // endregion 默认更新地址
    }

    private static <T> boolean SetRemoteHawkConfig(String hawkConfigName, T remoteValue, String remoteTips){
        boolean isPut = false;
        T oldValue = null;
        if (Hawk.contains(hawkConfigName)) {
            oldValue = Hawk.get(hawkConfigName);
            if (remoteValue instanceof Integer) {
                if (Hawk.get(hawkConfigName, 0) == 0)
                    isPut = true;
            } else if (remoteValue instanceof String) {
                if (Hawk.get(hawkConfigName, "").isEmpty())
                    isPut = true;
            } else if (remoteValue instanceof Boolean) {
                if (!Hawk.get(hawkConfigName, false))
                    isPut = true;
            }
        }else{
            isPut = true;
        }
        if(isPut){
            LOG.i("RemoteConfig",  "★远端设置", remoteTips, "老值："+oldValue, "新值："+remoteValue);
            Hawk.put(hawkConfigName, remoteValue);
            return true;
        }else
            LOG.i("RemoteConfig", "☆忽略远端", remoteTips, "保留值："+oldValue, "忽略值："+remoteValue);
        return false;
    }
}
