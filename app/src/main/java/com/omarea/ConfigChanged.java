package com.omarea;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class ConfigChanged extends BroadcastReceiver {
    @SuppressLint("ApplySharedPref")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("VAddins", "收到广播");
        //PendingResult pendingResult = goAsync();
        try {
            Bundle data = intent.getExtras();
            if (data == null || !data.containsKey("packageName")) {
                Log.e("VAddins", "packageName 未提供！");
                return;
            }
            String packageName = data.getString("packageName");

            @SuppressLint("WorldReadableFiles")
            SharedPreferences.Editor editor = context.getSharedPreferences("xposed", Context.MODE_WORLD_READABLE).edit();

            if (data.containsKey("hide_recent")) {
                boolean hide = data.getBoolean("hide_recent", false);
                if (hide) {
                    editor.putBoolean(packageName + "_hide_recent", true);
                } else {
                    editor.remove(packageName + "_hide_recent");
                }
            }
            if (data.containsKey("dpi")) {
                int dpi = data.getInt("dpi", 0);
                if (dpi > 0) {
                    editor.putInt(packageName+ "_dpi", dpi);
                } else {
                    editor.remove(packageName+ "_dpi");
                }
            }
            if (data.containsKey("scroll")) {
                boolean scroll = data.getBoolean("scroll", false);
                if (scroll) {
                    editor.putBoolean(packageName+ "_scroll", true);
                } else {
                    editor.remove(packageName+ "_scroll");
                }
            }
            editor.commit();
            Log.e("VAddins", "配置已更新");
            //pendingResult.setResultCode(0);
        } catch (Exception ex) {
            Log.e("VAddins", "配置更新失败");
            //pendingResult.setResultCode(-1);
        } finally {
            //pendingResult.finish();
        }
    }
}
