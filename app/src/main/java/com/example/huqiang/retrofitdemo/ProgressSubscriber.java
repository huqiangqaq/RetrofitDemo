package com.example.huqiang.retrofitdemo;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subscribers.ResourceSubscriber;
import retrofit2.HttpException;

/**
 * Created by 83916 on 2018/1/1.
 */

public abstract class ProgressSubscriber<T> extends ResourceSubscriber<T> implements ProgressCancelListener {

    private SimpleLoadingDialog dialogHandler;
    private Disposable disposable;

    public ProgressSubscriber(Context context) {
        dialogHandler = new SimpleLoadingDialog(context,this,true);
        showProgressDialog();
    }

    @Override
    public void onComplete() {
        dismissProgressDialog();
    }


    /**
     * 显示Dialog
     */
    public void showProgressDialog(){
        if (dialogHandler != null) {
            dialogHandler.obtainMessage(SimpleLoadingDialog.SHOW_PROGRESS_DIALOG).sendToTarget();
        }
    }

    @Override
    public void onNext(T t) {
        _onNext(t);
        dismissProgressDialog();
    }

    /**
     * 隐藏Dialog
     */
    private void dismissProgressDialog(){
        if (dialogHandler != null) {
            dialogHandler.obtainMessage(SimpleLoadingDialog.DISMISS_PROGRESS_DIALOG).sendToTarget();
            dialogHandler=null;
        }
    }
    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
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
        dismissProgressDialog();
    }


    @Override
    public void onCancelProgress() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    protected abstract void _onNext(T t);
    protected abstract void _onError(String message);

}
