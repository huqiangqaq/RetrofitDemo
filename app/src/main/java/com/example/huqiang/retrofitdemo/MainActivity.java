package com.example.huqiang.retrofitdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.zhpan.idea.net.common.Constants;
import com.zhpan.idea.net.common.DefaultObserver;
import com.zhpan.idea.net.download.DownloadListener;
import com.zhpan.idea.net.download.DownloadUtils;
import com.zhpan.idea.utils.LogUtils;
import com.zhpan.idea.utils.RxUtil;
import com.zhpan.idea.utils.ToastUtils;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;

import okhttp3.ResponseBody;

public class MainActivity extends RxAppCompatActivity {
    private Button btn,btn2,btn3,btn4;
    private String url = "http://imtt.dd.qq.com/16891/272C34139E6EDDBF34D6DD5109692C98.apk?fsname=com.goldensoft.wwms_1.8.7_187.apk&csr=1bbd";
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        dialog = new ProgressDialog(MainActivity.this);
        //普通请求
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetUtils.getApiService().getWxArticle()
                        .compose(RxUtil.<ResponseBody>rxSchedulerHelper(MainActivity.this))
                        .subscribe(new DefaultObserver<ResponseBody>() {
                            @Override
                            public void onSuccess(ResponseBody response) {
                                try {
                                    Toast.makeText(MainActivity.this,response.string().toString(),Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                            }
                        });

//               RetrofitHelper.createApi(MyApiService.class)
//                       .getWxArticle()
//                       .compose(RxHelper.<ResponseBody>transformer())
//                       .compose(MainActivity.this.<ResponseBody>bindUntilEvent(ActivityEvent.DESTROY))
//                       .subscribe(new RxSubscriber<ResponseBody>() {
//                           @Override
//                           public void onSuccess(ResponseBody responseBody) {
//                               try {
//                                   Toast.makeText(MainActivity.this,responseBody.string().toString(),Toast.LENGTH_LONG).show();
//                               } catch (IOException e) {
//                                   e.printStackTrace();
//                               }
//                           }
//
//                           @Override
//                           public void onFinished() {
//
//                           }
//                       });

            }
        });

        //带进度条
//        btn2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                RetrofitHelper.createApi(MyApiService.class)
//                        .getTopMovies(0,10)
//                        .compose(RxHelper.<Movie>transformer())
//                        .compose(MainActivity.this.<Movie>bindUntilEvent(ActivityEvent.DESTROY))
//                        .subscribe(new ProgressSubscriber<Movie>(MainActivity.this) {
//                            @Override
//                            protected void _onNext(Movie movie) {
//                                Toast.makeText(MainActivity.this,movie.getTitle(),Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            protected void _onError(String message) {
//
//                            }
//                        });
//
//                 HttpUtils.getInstance().apiService.getTopMoviesResult(0,10)
//                        .compose(RxHelper.<Movie>handleResult())
//                        .compose(MainActivity.this.<Movie>bindToLifecycle())
//                        .subscribe(new RxSubscriber<Movie>() {
//                            @Override
//                            public void onSuccess(Movie movie) {
//
//                            }
//
//                            @Override
//                            public void onFinished() {
//
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                super.onError(e);
//                            }
//                        });
//            }
//        });

        //测试多个ApiService
//        btn3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                HttpUtils.getInstance().testApi.getTopMovies(0,10)
//                        .compose(RxHelper.<Movie>transformer())
//                        .compose(MainActivity.this.<Movie>bindUntilEvent(ActivityEvent.STOP))
//                        .subscribe(new ProgressSubscriber<Movie>(MainActivity.this) {
//                            @Override
//                            protected void _onNext(Movie movie) {
//                                Toast.makeText(MainActivity.this,movie.getTitle(),Toast.LENGTH_LONG).show();
//                            }
//
//                            @Override
//                            protected void _onError(String message) {
//                            }
//                        });
//            }
//        });

        //下载
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
                dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
                dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
                dialog.setIcon(R.mipmap.ic_launcher);// 设置提示的title的图标，默认是没有的
                dialog.setTitle("提示");
                dialog.setMax(100);
                dialog.setMessage("这是一个水平进度条");
                dialog.setProgress(0);
                dialog.show();
                new DownloadUtils().download(url, new DownloadListener() {
                    @Override
                    public void onProgress(int progress) {
                        LogUtils.e("--------下载进度：" + progress);
                        Log.e("onProgress", "是否在主线程中运行:" + String.valueOf(Looper.getMainLooper() == Looper.myLooper()));
                        dialog.setProgress(progress);
                    }

                    @Override
                    public void onSuccess(ResponseBody responseBody) {  //  运行在子线程
//                        saveFile(responseBody);
                        Log.e("onSuccess", "是否在主线程中运行:" + String.valueOf(Looper.getMainLooper() == Looper.myLooper()));
                    }

                    @Override
                    public void onFail(String message) {
                        btn.setClickable(true);
//                        ToastUtils.show("文件下载失败,失败原因：" + message);
                        Log.e("onFail", "是否在主线程中运行:" + String.valueOf(Looper.getMainLooper() == Looper.myLooper()));
                    }

                    @Override
                    public void onComplete() {  //  运行在主线程中
                        ToastUtils.show("文件下载成功");
                        btn.setClickable(true);
                    }
                });
//                HttpUtils.getInstance().apiService.downloadFile(url)
//                        .compose(RxHelper.<ResponseBody>downloadTransformer())
//                        .compose(MainActivity.this.<ResponseBody>bindToLifecycle())
//                        .subscribe(new DownLoadSubscriber<ResponseBody>(MainActivity.this, new CallBack() {
//
//                            @Override
//                            public void onStart() {
//                                super.onStart();
//                                Toast.makeText(MainActivity.this, url+"  is  start", Toast.LENGTH_SHORT).show();
//
//                                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
//                                dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
//                                dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
//                                dialog.setIcon(R.mipmap.ic_launcher);// 设置提示的title的图标，默认是没有的
//                                dialog.setTitle("提示");
//                                dialog.setMax(100);
//                                dialog.setMessage("这是一个水平进度条");
//                                dialog.setProgress(0);
////                                dialog.show();
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                Toast.makeText(MainActivity.this,  " error", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onProgress(int fileSizeDownloaded) {
//                                super.onProgress(fileSizeDownloaded);
//                                dialog.setProgress(fileSizeDownloaded);
//                                Log.i("huqiang",fileSizeDownloaded+"");
//                                btn.setText(fileSizeDownloaded+"");
//
//                            }
//
//                            @Override
//                            public void onSucess(String path, String name, long fileSize) {
//                                Toast.makeText(MainActivity.this, name + " is  downLoaded", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            }
//                        }));
            }
        });
//        btn4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                HttpUtils.getInstance().apiService.downloadFile(url)
//                        .compose(RxHelper.<ResponseBody>transformer())
//                        .compose(MainActivity.this.<ResponseBody>bindToLifecycle())
//                        .subscribe(new DownLoadSubscriber<ResponseBody>(MainActivity.this, new CallBack() {
//
//                            @Override
//                            public void onStart() {
//                                super.onStart();
//                                Toast.makeText(MainActivity.this, url+"  is  start", Toast.LENGTH_SHORT).show();
//
//                                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
//                                dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
//                                dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
//                                dialog.setIcon(R.mipmap.ic_launcher);// 设置提示的title的图标，默认是没有的
//                                dialog.setTitle("提示");
//                                dialog.setMax(100);
//                                dialog.setMessage("这是一个水平进度条");
//                                dialog.setProgress(0);
////                                dialog.show();
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                Toast.makeText(MainActivity.this,  " error", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onProgress(int fileSizeDownloaded) {
//                                super.onProgress(fileSizeDownloaded);
//                                dialog.setProgress(fileSizeDownloaded);
//                                Log.i("huqiang",fileSizeDownloaded+"");
//                                btn.setText(fileSizeDownloaded+"");
//
//                            }
//
//                            @Override
//                            public void onSucess(String path, String name, long fileSize) {
//                                Toast.makeText(MainActivity.this, name + " is  downLoaded", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            }
//                        }));
//
//            }
//        });

    }
}
