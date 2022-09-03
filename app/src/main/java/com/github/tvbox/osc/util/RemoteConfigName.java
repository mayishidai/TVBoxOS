package com.github.tvbox.osc.util;

// 远程配置文件名
public class RemoteConfigName {
    public static final String IsRecodeLog="IsRecodeLog"; // 是否开启日志记录
    // region 默认配置相关
    public static final String APIUrl="APIUrl"; // 默认首页API地址
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
}
