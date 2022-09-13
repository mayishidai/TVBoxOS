package com.github.tvbox.osc.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alibaba.android.vlayout.layout.FixAreaLayoutHelper;
import com.github.other.xunfei.WebIATWS;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.AppManager;
import com.github.tvbox.osc.util.LOG;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.thoughtworks.xstream.mapper.Mapper;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// 子线程运行主线程方法
//1.通过主线程Handler的post方法2.通过主线程Handler的sendMessage方法3.通过Activity的runOnUiThread方法4.通过View的post方法
public class VoiceDialog extends BaseDialog {
    Context mContext;
    String audioDir = "audio";
    // 录音界面相关
    Button btnStart;
    Button btnStop;
    TextView textTime;
    ImageView mIdIvRecode;
    // 录音功能相关
    MediaRecorder mMediaRecorder; // MediaRecorder 实例
    AudioRecord audioRecord; // AudioRecord 实例
    ThreadPoolExecutor mExecutorService;// AudioRecord 执行器
    boolean isRecording; // 录音状态
    String fileName; // 录音文件的名称
    String filePath; // 录音文件存储路径
    String cacheFilePath; // 录音文件存储路径(缓存)
    Thread timeThread; // 记录录音时长的线程
    int timeCount; // 录音时长 计数
    int maxTimeCount = 59; // 最大录音时长 计数
    final int TIME_COUNT = 0x101;
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_COUNT:
                    int count = (int) msg.obj;
                    LOG.d("VoiceDialog", "time == " + count);
                    textTime.setText(String.format("%s",maxTimeCount-count+1));
                    if (timeCount >= maxTimeCount){
                        btnStop.performClick();
                    }
                    break;
            }
        }
    };
    AnimationDrawable animation; // 录音动画

    public static Handler.Callback textCallback;
    private static VoiceDialog instance;
    public static VoiceDialog getInstance(){
        return instance;
    }

    // 构造
    public VoiceDialog(@NonNull @NotNull Context context) {
        super(context);
        this.setCancelable(true);
        instance = this;
        mContext = context;
        mExecutorService = new ThreadPoolExecutor(3, 5, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>(10));
        setContentView(R.layout.dialog_voice);
        setCanceledOnTouchOutside(false);

        audioDir = context.getExternalFilesDir(audioDir).getAbsolutePath();
        initFile();

        mIdIvRecode = findViewById(R.id.mIdIvRecode);
        mIdIvRecode.setBackgroundResource(R.drawable.anim_voice_play);
        animation = (AnimationDrawable) mIdIvRecode.getBackground();
        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        textTime = (TextView) findViewById(R.id.text_time);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation.start();

                // 开始录音
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);
                startRecord();
                isRecording = true;
                // 初始化录音时长记录
                timeThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        countTime();
                    }
                });
                timeThread.start();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animation.stop();
                animation.selectDrawable(0);

                // 停止录音
                btnStart.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.GONE);
                stopRecord();
                isRecording = false;
                startVoiceToText();
            }
        });
        btnStop.setVisibility(View.GONE);
        textTime.setVisibility(View.GONE);

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                onDismissHandle(dialogInterface);
            }
        });
    }

    // 设置文本获取回调
    public void SetTextCallback(Handler.Callback textCallback){
        VoiceDialog.textCallback = textCallback;
    }

    private void initFile() {
        //录音文件
        File audioFile = new File(audioDir);
        if (!audioFile.exists()) {
            audioFile.mkdirs();
        } else if (!audioFile.isDirectory()) {
            audioFile.delete();
            audioFile.mkdirs();
        }
    }

    // 记录录音时长
    private void countTime() {
        while (isRecording) {
            LOG.d("VoiceDialog", "正在录音");
            timeCount++;
            Message msg = Message.obtain();
            msg.what = TIME_COUNT;
            msg.obj = timeCount;
            myHandler.sendMessage(msg);
            try {
                timeThread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.printStackTrace(e);
            }
        }
        LOG.d("VoiceDialog", "结束录音");
        timeCount = 0;
        Message msg = Message.obtain();
        msg.what = TIME_COUNT;
        msg.obj = timeCount;
        myHandler.sendMessage(msg);
    }

    // 开始录音
    public void startRecord() {
        Toast.makeText(mContext, "开始录音", Toast.LENGTH_SHORT).show();
        //startRecordARM(filePath);
        mExecutorService.execute(() -> {
            VoiceDialog.getInstance().startRecordPCM();
        });
    }

    /**
     * 开始录音 使用amr格式
     * <p>
     * 录音文件
     *
     * @return
     */
    public void startRecordARM() {
        //注意文件夹要创建之后才能使用
        fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".m4a";
        filePath = String.format("%s/%s", audioDir, fileName);
        cacheFilePath = filePath;

        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            /* ③准备 */
            mMediaRecorder.setOutputFile(filePath);
            /* 持续时间 */
            mMediaRecorder.setMaxDuration(60*1000);
            mMediaRecorder.prepare();
            /* ④开始 */
            mMediaRecorder.start();
        } catch (IllegalStateException e) {
            LOG.e("VoiceDialog", "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.e("VoiceDialog", "call startAmr(File mRecAudioFile) failed!" + e.getMessage());
        }
    }
    /**
     * 开始录音 使用pcm格式
     * <p>
     * 录音文件
     *
     * @return
     */
    public void startRecordPCM(){
        //注意文件夹要创建之后才能使用
        fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".pcm";
        filePath = String.format("%s/%s", audioDir, fileName);
        cacheFilePath = filePath;

        try {
            //输出流
            OutputStream os = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            /**
             * android.media.AudioRecord public static int getMinBufferSize(int
             sampleRateInHz,int channelConfig,int audioFormat)
             返回成功创建 AudioRecord 对象所需的最小缓冲区大小，以字节为单位。 请注意，此大小
             不能保证在负载下顺利录制，应根据 AudioRecord 实例轮询新数据的预期频率选择更高的
             值。 有关有效配置值的更多信息，请参阅AudioRecord(int, int, int, int, int) 。
             参数：
             sampleRateInHz – 以赫兹表示的采样率。 AudioFormat.SAMPLE_RATE_UNSPECIFIED是不允许的。  16000表示16K采集率
             channelConfig – 描述音频通道的配置。 请参阅AudioFormat.CHANNEL_IN_MONO和
             AudioFormat.CHANNEL_IN_STEREO
             audioFormat – 表示音频数据的格式。 请参阅AudioFormat.ENCODING_PCM_16BIT 。
             回报：
             ERROR_BAD_VALUE如果硬件不支持录制参数，或者传递了无效参数，或者如果实现无法查询
             硬件以获取其输入属性或以字节表示的最小缓冲区大小，则为ERROR
             */
            int bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            byte[] buffer = new byte[bufferSize];
            LOG.d("VoiceDialog", "===startRecord===" + audioRecord.getState());
            audioRecord.startRecording();

            isRecording = true;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.write(buffer[i]);
                }
            };

            audioRecord.stop();
            audioRecord.release();
            dos.close();
        } catch (Throwable t) {
            LOG.e("VoiceDialog", "录音失败");
            Toast.makeText (mContext, "录音失败", Toast.LENGTH_SHORT);
            LOG.printStackTrace(t);
        }
    }

    // 停止录音
    public void stopRecord() {
        //Toast.makeText(mContext, "结束录音", Toast.LENGTH_SHORT).show();
        //stopRecordAMR();
        stopRecordPCM();
    }

    // 停止录音AMR
    public void stopRecordAMR() {
        //有一些网友反应在5.0以上在调用stop的时候会报错，翻阅了一下谷歌文档发现上面确实写的有可能会报错的情况，捕获异常清理一下就行了，感谢大家反馈！
        try {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            filePath = "";

        } catch (RuntimeException e) {
            LOG.printStackTrace(e);
            LOG.e("VoiceDialog", e.toString());
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            File file = new File(filePath);
            if (file.exists())
                file.delete();
            filePath = "";
        }
    }
    // 停止录音PCM
    public void stopRecordPCM() {
        isRecording = false;
    }
    // 开始执行语音转文字
    private void startVoiceToText() {
        LOG.d("VoiceDialog", "开始语音转文字", cacheFilePath);
        //Toast.makeText(mContext, "语音转换文字中，请耐心等待！", Toast.LENGTH_SHORT).show();
        ProgressDialog progressDialog = AppManager.getInstance().showProgressDialog("语音转换文字中...", null);
        try {
            WebIATWS.StartConvert(cacheFilePath, new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message message) {
                    if (VoiceDialog.textCallback!=null)
                    {
                        VoiceDialog.textCallback.handleMessage(message);
                    }
                    progressDialog.dismiss();
                    if (!message.obj.toString().isEmpty()){
                        VoiceDialog.getInstance().dismiss();
                    }
                    return true;
                }
            });
        } catch (Exception ex) {
            LOG.printStackTrace(ex);
        }
    }

    // 格式化 录音时长为 秒
    public static String FormatMiss(int miss) {
        return "" + miss;
    }

    // 录音权限处理
    public static void voicePermissions(Context mContext, Handler.Callback callback) {
        if (XXPermissions.isGranted(mContext, Permission.RECORD_AUDIO)) {
            Toast.makeText(mContext, "已获得录音权限", Toast.LENGTH_SHORT).show();
            callback.handleMessage(null);
        } else {
            XXPermissions.with(mContext)
                    .permission(Permission.RECORD_AUDIO)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                Toast.makeText(mContext, "已获得录音权限", Toast.LENGTH_SHORT).show();
                                callback.handleMessage(null);
                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                Toast.makeText(mContext, "获取存储权限失败,请在系统设置中开启", Toast.LENGTH_SHORT).show();
                                XXPermissions.startPermissionActivity((Activity) mContext, permissions);
                            } else {
                                Toast.makeText(mContext, "获取存储权限失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // 释放UI处理
    private void onDismissHandle(DialogInterface dialogInterface) {
        myHandler.removeCallbacksAndMessages(null);
        instance = null;
        mExecutorService.shutdown();
        mExecutorService = null;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dismiss();
    }
}