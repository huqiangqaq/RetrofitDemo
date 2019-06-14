package com.example.huqiang.retrofitdemo;

import android.content.Context;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.ResourceSubscriber;

/**
 * Created by huqiang on 2018/3/27 15:48.
 */

public class DownLoadSubscriber<ResponseBody> implements Observer<ResponseBody> {
    CallBack callBack;
    Context mContext;
    public DownLoadSubscriber(Context context,CallBack callBack) {
        this.callBack = callBack;
        this.mContext = context;
    }


    @Override
    public void onComplete() {
        if (callBack != null) {
            callBack.onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (callBack != null) {
            callBack.onError(e);
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (callBack != null) {
            callBack.onStart();
        }
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        DownloadManager.getInstance(callBack).writeResponseBodyToDisk(mContext, (okhttp3.ResponseBody) responseBody);

    }
}

