package com.github.tvbox.osc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.tvbox.osc.ui.activity.HomeActivity;
import com.github.tvbox.osc.util.LOG;

/// 开机自启动
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.e("TVBox", "BootCompleteReceiver onReceive  1 ");
        LOG.e("BootCompleteReceiver", "onReceive", 1);
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            LOG.e("BootCompleteReceiver", "onReceive", 2);
            Intent thisIntent = new Intent(context, HomeActivity.class);
            thisIntent.setAction("android.intent.action.MAIN");
            thisIntent.addCategory("android.intent.category.LAUNCHER");
            thisIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            LOG.e("BootCompleteReceiver", "onReceive", 3);
            context.startActivity(thisIntent);
            LOG.e("BootCompleteReceiver", "onReceive", 4);
        }
    }
}