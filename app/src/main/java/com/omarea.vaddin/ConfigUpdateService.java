package com.omarea.vaddin;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

public class ConfigUpdateService extends Service {
    public ConfigUpdateService() {

    }

    private IBinder binder;

    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null) {
            binder = new StudentQueryBinder(this);
        }
        return binder;
    }

    class StudentQueryBinder extends IAppConfigAidlInterface.Stub {
        Context context;

        StudentQueryBinder(Context context) {
            this.context = context;
        }

        @Override
        public int getVersion() {
            try {
                PackageManager manager = context.getPackageManager();
                int code = 0;
                try {
                    PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                    code = info.versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                return code;
            } catch (Exception ex) {
                Log.e("getVersion", ex.getLocalizedMessage());
                return -1;
            }
        }

        @Override
        public String getAppConfig(String packageName) {
            JSONObject jsonObject = new JSONObject();
            try {
                SharedPreferences sharedPreferences = context.getSharedPreferences("xposed", Context.MODE_WORLD_READABLE);
                jsonObject.put("excludeRecent", sharedPreferences.getBoolean(packageName + "_hide_recent", false));
                jsonObject.put("smoothScroll", sharedPreferences.getBoolean(packageName + "_scroll", false));
                jsonObject.put("dpi", sharedPreferences.getInt(packageName + "_dpi", 0));
            } catch (Exception ex) {
            }
            return jsonObject.toString();
        }

        @Override
        public boolean updateAppConfig(String packageName, int dpi, boolean excludeRecent, boolean smoothScroll) {
            if (packageName == null) {
                return false;
            }
            if (packageName.isEmpty()) {
                return false;
            }

            try {
                @SuppressLint("WorldReadableFiles")
                SharedPreferences.Editor editor = context.getSharedPreferences("xposed", Context.MODE_WORLD_READABLE).edit();

                if (excludeRecent)
                    editor.putBoolean(packageName + "_hide_recent", true);
                else
                    editor.remove(packageName + "_hide_recent");

                if (dpi >= 96)
                    editor.putInt(packageName + "_dpi", dpi);
                else
                    editor.remove(packageName + "_dpi");

                if (smoothScroll)
                    editor.putBoolean(packageName + "_scroll", true);
                else
                    editor.remove(packageName + "_scroll");

                editor.commit();
                Log.e("VAddins", "配置已更新");
                Toast.makeText(context, "当前应用Xposed配置已更新", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
