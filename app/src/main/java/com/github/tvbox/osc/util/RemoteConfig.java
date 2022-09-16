package com.github.tvbox.osc.util;

import android.content.Intent;
import android.os.Bundle;

import com.github.tvbox.osc.base.App;
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

        // 日志
        if (GetValue(RemoteConfigName.IsRecodeLog)!=null && GetValue(RemoteConfigName.IsRecodeLog).getAsBoolean()) {
            LOG.i("RemoteConfig", "远端打开日志保存");
            LOG.OpenSaveLog();
        }
        else {
            LOG.i("RemoteConfig", "远端关闭日志保存");
            LOG.ClsoeSaveLog();
        }

        // 默认API地址
        if (GetValue(RemoteConfigName.APIUrl)!=null && !GetValue(RemoteConfigName.APIUrl).getAsString().isEmpty()) {
            String apiUrl = GetValue(RemoteConfigName.APIUrl).getAsString();
            LOG.i("RemoteConfig", "远端设置首页API", apiUrl);
            if(Hawk.get(HawkConfig.API_URL, "").isEmpty()){
                LOG.i("RemoteConfig", "开始设置远端首页配置");
                Hawk.put(HawkConfig.API_URL, apiUrl);
                Bundle bundle = new Bundle();
                bundle.putBoolean("useCache", true);
                Intent intent = new Intent(App.getInstance().getBaseContext(), HomeActivity.class);
                intent.putExtras(bundle);
                App.getInstance().startActivity(intent);
            }else
                LOG.i("RemoteConfig", "本地有首页，忽略远端首页配置");
        }

        // 默认更新地址
        if (GetValue(RemoteConfigName.UpdateUrl)!=null && !GetValue(RemoteConfigName.UpdateUrl).getAsString().isEmpty()) {
            String updateUrl = GetValue(RemoteConfigName.UpdateUrl).getAsString();
            LOG.i("RemoteConfig", "远端设置默认更新地址", updateUrl);
            if (GetValue(RemoteConfigName.IsForceUpdate)!=null && GetValue(RemoteConfigName.IsForceUpdate).getAsBoolean()){
                LOG.i("RemoteConfig", "远端设置启动强制显示更新");
                UpdateDialog.checkUpdate(AppManager.getInstance().currentActivity(), false);
            }else {
                LOG.i("RemoteConfig", "远端设置启动非强制显示更新");
            }
        }
    }
}
