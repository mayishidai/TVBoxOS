package com.github.tvbox.osc.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.github.tvbox.osc.data.CustomData;
import com.orhanobut.hawk.Hawk;

import java.io.File;
import java.util.Locale;

public class TTSService {
    private static TTSService singleton;
    private Context mContext;
    //核心播放对象
    private TextToSpeech textToSpeech;
    private boolean isSupport = false;
    private static final String TAG = "SystemTTS";
    private boolean isFirstPlay = false;

    private void speak(String text){
        /*
            TextToSpeech的speak方法有两个重载。
            // 执行朗读的方法
            speak(CharSequence text,int queueMode,Bundle params,String utteranceId);
            // 将朗读的的声音记录成音频文件
            synthesizeToFile(CharSequence text,Bundle params,File file,String utteranceId);
            第二个参数queueMode用于指定发音队列模式，两种模式选择
            （1）TextToSpeech.QUEUE_FLUSH：该模式下在有新任务时候会清除当前语音任务，执行新的语音任务
            （2）TextToSpeech.QUEUE_ADD：该模式下会把新的语音任务放到语音任务之后，
            等前面的语音任务执行完了才会执行新的语音任务
        */
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d(TAG, "播放开始");
            }

            @Override
            public void onDone(String utteranceId) {
                Log.d(TAG, "播放结束");
            }

            @Override
            public void onError(String utteranceId) {
                Log.d(TAG, "播放出错");
            }
        });
    }

    //textToSpeech的配置
    private void initTTs(int status) {
        /*
                使用的是小米手机进行测试，打开设置，在系统和设备列表项中找到更多设置，
            点击进入更多设置，在点击进入语言和输入法，见语言项列表，点击文字转语音（TTS）输出，
            首选引擎项有三项为Pico TTs，科大讯飞语音引擎3.0，度秘语音引擎3.0。其中Pico TTS不支持
            中文语言状态。其他两项支持中文。选择科大讯飞语音引擎3.0。进行测试。

            	如果自己的测试机里面没有可以读取中文的引擎，
            那么不要紧，我在该Module包中放了一个科大讯飞语音引擎3.0.apk，将该引擎进行安装后，进入到
            系统设置中，找到文字转语音（TTS）输出，将引擎修改为科大讯飞语音引擎3.0即可。重新启动测试
            Demo即可体验到文字转中文语言。
         */
        isSupport = false;
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINESE);
            // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //系统不支持中文播报
                Toast.makeText(this.mContext, "TTS 数据丢失或不支持", Toast.LENGTH_SHORT).show();
            } else {
                isSupport = true;
                textToSpeech.setPitch(1.0f);
                textToSpeech.setSpeechRate(1.0f);
            }
            Log.i(this.getClass().getName(), "onInit: TextToSpeech 初始化成功  "+ status);
        }else{
            Log.e(this.getClass().getName(), "onInit: TextToSpeech 初始化失败  "+ status);
        }
        if (!isSupport){
            Log.e(this.getClass().getName(), "initTTs: 失败，需要下载软件科大讯飞语音引擎3.0.apk，安装并设置");

        }
    }

    public static TTSService getInstance() {
        if (singleton == null) {
            synchronized (TTSService.class) {
                if (singleton == null) {
                    singleton = new TTSService();
                }
            }
        }
        return singleton;
    }

    public boolean play(String text) {
        if (!isOpenConfig()){
            return false;
        }
        boolean ret=false;
        if (!isCanUse()) {
            Toast.makeText(mContext, "TTS不支持", Toast.LENGTH_SHORT).show();
            ret=true;
        }
        if (textToSpeech != null) {
            if(!isFirstPlay){
                speak(text);
                isFirstPlay = true;
                ret=true;
            }else{
                if(!textToSpeech.isSpeaking()){
                    speak(text);
                    ret=true;
                }else{
                    stop();
                    speak(text);
                    ret=true;
                }
            }
        }
        return  ret;
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
    //是否正在说话
    public boolean isSpeeking() {
        return textToSpeech.isSpeaking();
    }
    //是否支持TTS
    public boolean isSupport(){
        return isSupport;
    }
    //是否开放配置
    public boolean isOpenConfig() {
        return Hawk.get(HawkConfig.TTS, false);
    }
    //是否 一定能使用TTS
    public boolean isCanUse() {
        return isSupport() && isOpenConfig();
    }
    //是否已安装TTS
    public boolean isInstalled(){
        if(isSupport())
            return true;

        PackageInfo packageInfo;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo("com.iflytek.speechcloud", 0);
        }catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        if(packageInfo ==null){
            return false;
        }else{
            uninit();
            init(mContext);
            return true;
        }
    }
    public String downUrl(){
        return CustomData.getInstance().GetTTSDownloadUrl();
    }
    public boolean isDownloadExist(Context context) {
        if (!new File(getDownloadPath()).exists())
            return false;
        return true;
    }

    public String getDownloadPath() {
        String dir = mContext.getCacheDir().getAbsolutePath();
        return dir + "/" + saveFile();
    }
    public String saveFile() {
        return String.format("tts.apk");
    }

    public void init(Context context){
        this.mContext = context.getApplicationContext();
        textToSpeech = new TextToSpeech(mContext, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //textToSpeech的配置
                initTTs(status);
                isFirstPlay = false;
            }
        });
    }
    public void uninit(){
        stop();
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        textToSpeech = null;
    }
}