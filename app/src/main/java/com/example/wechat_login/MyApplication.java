package com.example.wechat_login;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WxLogin.initWx(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        WxLogin.destroy(getApplicationContext());
    }
}
