package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.LOG;
import com.github.tvbox.osc.util.TTSService;
import com.github.tvbox.osc.util.ToolUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class TTSInitDialog extends BaseDialog {
    private OnListener listener;
    private TextView downText;

    public TTSInitDialog(@NonNull @NotNull Context context) {
        super(context);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
        setContentView(R.layout.dialog_tts);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OkGo.getInstance().cancelTag("down_tts");
            }
        });
        downText = findViewById(R.id.downTTS);
        TextView downTip = findViewById(R.id.downTTSArch);

        downTip.setText("下载TTSView运行组件");

        if (TTSService.getInstance().isDownloadExist(context)) {
            downText.setText("重新安装");
        }else{
            downText.setText("下载并安装");
        }

        downText.setOnClickListener(new View.OnClickListener() {

            private void setTextEnable(boolean enable) {
                downText.setEnabled(enable);
                downText.setTextColor(enable ? Color.BLACK : Color.GRAY);
            }

            @Override
            public void onClick(View v) {
                FastClickCheckUtil.check(v);
                setTextEnable(false);
                if (TTSService.getInstance().isDownloadExist(context)) {
                    ToolUtils.installApk(context, new File(TTSService.getInstance().getDownloadPath()));
                    dismiss();
                    return;
                }
                OkGo.<File>get(TTSService.getInstance().downUrl()).tag("down_tts").execute(new FileCallback(context.getCacheDir().getAbsolutePath(), TTSService.getInstance().saveFile()) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        try {
                            LOG.d(this, "TTS下载在本地地址", response.body().getAbsolutePath());
                            ToolUtils.installApk(context, response.body().getAbsoluteFile());
                            downText.setText("重新下载");
                            if (listener != null)
                                listener.onchange();
                            dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                            setTextEnable(true);
                        }
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        Toast.makeText(context, response.getException().getMessage(), Toast.LENGTH_LONG).show();
                        setTextEnable(true);
                    }

                    @Override
                    public void downloadProgress(Progress progress) {
                        super.downloadProgress(progress);
                        downText.setText(String.format("%.2f%%", progress.fraction * 100));
                    }
                });
            }
        });
    }

    public void OnPerformClick(){
        downText.performClick();
    }

    public TTSInitDialog setOnListener(OnListener listener) {
        this.listener = listener;
        return this;
    }

    public interface OnListener {
        void onchange();
    }
}