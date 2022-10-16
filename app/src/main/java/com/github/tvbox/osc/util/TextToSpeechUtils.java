package com.github.tvbox.osc.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class TextToSpeechUtils {
    private static TextToSpeechUtils textToSpeechUtils;
    private TextToSpeech mTextToSpeech;    // TTS对象
    private boolean isInitSuccess;

    public static TextToSpeechUtils getInstance() {
        if (textToSpeechUtils == null) {
            textToSpeechUtils = new TextToSpeechUtils();
        }
        return textToSpeechUtils;
    }

    private TextToSpeechUtils() {
    }

    public void initTextToSpeech(Context context) {
        if (isInitSuccess){
            return;
        }
        mTextToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
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
                    isInitSuccess = true;
                    LOG.d("TextToSpeechUtils", "语音TTS识别初始化成功");
                    // setLanguage设置语言
                    int result = mTextToSpeech.setLanguage(Locale.CHINA);
                    // TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失
                    // TextToSpeech.LANG_NOT_SUPPORTED：不支持
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    isInitSuccess = true;
                    LOG.e("TextToSpeechUtils", "语音TTS识别初始化失败");
                    Toast.makeText(context, "语音TTS识别初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        mTextToSpeech.setPitch(1.0f);
        // 设置语速
        mTextToSpeech.setSpeechRate(1.0f);
    }

    public void close(){
        if (!isInitSuccess){
            return;
        }
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();        // 不管是否正在朗读TTS都被打断
            mTextToSpeech.shutdown();    // 关闭，释放资源
            mTextToSpeech = null;
        }
    }

    public void speak(String speakStr){
        if (!isInitSuccess){
            return;
        }
        if (mTextToSpeech != null && !mTextToSpeech.isSpeaking()) {
            mTextToSpeech.speak(speakStr, TextToSpeech.QUEUE_ADD, null);
        }
    }
}
