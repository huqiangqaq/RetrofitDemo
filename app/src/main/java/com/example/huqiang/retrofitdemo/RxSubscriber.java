package com.example.huqiang.retrofitdemo;

import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;

import io.reactivex.FlowableSubscriber;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.ResourceSubscriber;
import retrofit2.HttpException;

/**
 * Created by 83916 on 2018/1/1.
 */

public abstract class RxSubscriber<T> extends ResourceSubscriber<T> {
    private Disposable disposable;
    /**
     * 成功返回结果时被调用
     *
     * @param t
     */
    public abstract void onSuccess(T t);

    /**
     * 成功或失败到最后都会调用
     */
    public abstract void onFinished();

    @Override
    public void onComplete() {
        onFinished();
    }
    @Override
    public void onError(Throwable e) {
        String errorMsg;
        if (e instanceof IOException){
            //没有网络
            errorMsg = "请检查你的网络状态";
        }else if (e instanceof HttpException){
            /** 网络异常，http 请求失败，即 http 状态码不在 [200, 300) 之间, such as: "server internal error". */
            errorMsg = ((HttpException) e).response().message();
        }else if (e instanceof ApiException){
            /** 网络正常，http 请求成功，服务器返回逻辑错误 */
            errorMsg = e.getMessage();
        }else {
            /** 其他未知错误 */
            errorMsg = !TextUtils.isEmpty(e.getMessage()) ? e.getMessage() : "unknown error";
        }
        Toast.makeText(AppUtil.getContext(), errorMsg, Toast.LENGTH_SHORT).show();

        onFinished();
    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

//    @Override
//    public void onSubscribe(Disposable d) {
//        disposable = d;
//    }

    public Disposable getDisposable() {
        return disposable;
    }
}
