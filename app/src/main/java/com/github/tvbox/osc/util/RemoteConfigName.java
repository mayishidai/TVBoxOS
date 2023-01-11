package com.github.tvbox.osc.util;

// 远程配置文件名
public class RemoteConfigName extends HawkConfig {
    public static final String IsRecodeLog="IsRecodeLog"; // 是否开启日志记录

    // region 语音搜索
    public static final String VoiceAppID="VoiceAppID"; //讯飞AppID
    public static final String VoiceApiSecret="VoiceApiSecret"; //讯飞ApiSecret
    public static final String VoiceApiKey="VoiceApiKey"; //讯飞ApiKey
    // endregion

    // region TTS 文字转语音
    public static final String TTSDownLoad="TTSDownLoad"; //下载地址
    // endregion


    // region 直播配置相关
    public static final String Live ="Live"; //直播
    public static final String Live_Channel="Channel"; //频道名字
    public static final String Live_ChannelReverse = "ChannelReverse"; // 换台反转
    public static final String Live_CrossGroup = "CrossGroup"; // 跨选分类
    public static final String Live_ConnectTimeout = "ConnectTimeout"; // 超时换源时间 0 5s 1 10s 2 15s 3 20s 4 25s 5 30s
    public static final String Live_ShowNetSpeed = "ShowNetSpeed"; // 显示网速
    public static final String Live_ShowTime = "ShowTime"; // 显示时间
    // endregion





    // region 自定义数据
    public static final String CustomData="CustomData"; // 自定义数据

    //远程控制按钮
    public static final String CustomData_YOUND="CustomData_YOUND"; // 自定义数据 默认/青年版
    public static final String CustomData_AGED="CustomData_AGED"; // 自定义数据 老年版

    public static final String APPModel_HomeButtons="HomeButtons"; // 首页按钮列表
    public static final String HomeButtons_HistoryButton="HistoryButton"; // 历史按钮
    public static final String HomeButtons_LiveButton="LiveButton"; // 直播按钮
    public static final String HomeButtons_SearchButton="SearchButton"; // 搜索按钮
    public static final String HomeButtons_PushButton="PushButton"; // 推送按钮
    public static final String HomeButtons_CollectionButton="CollectionButton"; // 收藏按钮
    public static final String HomeButtons_SettingButton="SettingButton"; // 设置按钮
    public static final String HomeButtons_CloudGameButton="CloudGameButton"; // 云游戏按钮

    // region 默认配置相关
    public static final String ForceChangeAPIUrl="ForceChangeAPIUrl"; //强制替换首页默认API地址，防止API失效不更新包的手段
    public static final String APIUrl="APIUrl"; // 默认首页API地址
    public static final String ForceChangeHomeID="ForceChangeHomeID"; //强制替换首页ID
    public static final String HomeID="HomeID"; //默认首页数据源
    public static final String HomeShowType="HomeShowType"; //默认首页推荐
    public static final String HomeSearchType="HomeSearchType"; //默认搜索展示
    public static final String HomeFastSearch="HomeFastSearch"; //默认聚合模式
    public static final String HomeDNSType="HomeDNSType"; //默认安全DNS
    public static final String HomeHistoryNum="HomeHistoryNum"; //默认历史记录
    public static final String HomePictureZoom="HomePictureZoom"; //默认画面缩放
    public static final String HomeWindowPreview="HomeWindowPreview"; //默认窗口预览
    // endregion

    // region 更新相关
    public static final String IsForceUpdate="IsForceUpdate"; // 是否首页强制显示更新
    public static final String UpdateData="UpdateData"; // 更新数据
    public static final String UpdateData_NewVersion="NewVersion"; // 更新数据_新版本号
    public static final String UpdateData_ForceUpdate="ForceUpdate"; // 更新数据_是否强制更新
    public static final String UpdateData_UpdateDesc="UpdateDesc"; // 更新数据_更新描述
    public static final String UpdateData_UpdateDownloadUrl="UpdateDownloadUrl"; // 更新数据_更新下载地址
    // endregion

    // endregion
}
