package com.example.huqiang.retrofitdemo;

import android.app.Application;

import com.zhpan.idea.utils.Utils;

/**
 * Created by 83916 on 2018/1/1.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
