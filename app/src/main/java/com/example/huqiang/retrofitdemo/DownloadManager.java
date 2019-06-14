package com.example.huqiang.retrofitdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;

import okhttp3.ResponseBody;

/**
 * Created by huqiang on 2018/3/27 15:10.
 */

public class DownloadManager {
    private CallBack callBack;

    private static final String TAG = "DownLoadManager";

    private static String APK_CONTENTTYPE = "application/vnd.android.package-archive";

    private static String PNG_CONTENTTYPE = "image/png";

    private static String JPG_CONTENTTYPE = "image/jpg";

    private static String fileSuffix="";

    private Handler handler;

    public DownloadManager(CallBack callBack) {
        this.callBack = callBack;
    }

    private static DownloadManager sInstance;

    /**
     *DownLoadManager getInstance
     */
    public static synchronized DownloadManager getInstance(CallBack callBack) {
        if (sInstance == null) {
            sInstance = new DownloadManager(callBack);
        }
        return sInstance;
    }



    public boolean  writeResponseBodyToDisk(Context context, ResponseBody body) {

        Log.d(TAG, "contentType:>>>>"+ body.contentType().toString());

        String type = body.contentType().toString();

        if (type.equals(APK_CONTENTTYPE)) {

            fileSuffix = ".apk";
        } else if (type.equals(PNG_CONTENTTYPE)) {
            fileSuffix = ".png";
        } else if (type.equals(JPG_CONTENTTYPE)) {
            fileSuffix = ".jpg";
        }

        // 其他同上 自己判断加入

        final String name = System.currentTimeMillis() + fileSuffix;
        final String path = context.getExternalFilesDir(null) + File.separator + name;

        Log.d(TAG, "path:>>>>"+ path);

        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(path);

            if (futureStudioIconFile.exists()) {
                futureStudioIconFile.delete();
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[1024];

                final long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                Log.d(TAG, "file length: "+ fileSize);
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    // 创建一个数值格式化对象

                    NumberFormat numberFormat = NumberFormat.getInstance();

                    // 设置精确到小数点后2位

                    numberFormat.setMaximumFractionDigits(0);

                    final String result = numberFormat.format((float) fileSizeDownloaded / (float) fileSize * 100);
                    Log.d(TAG, "file download: " + result);
                    if (callBack != null) {
                        handler = new Handler(Looper.getMainLooper());
                        final long finalFileSizeDownloaded = fileSizeDownloaded;

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onProgress(Integer.parseInt(result));
                            }
                        });

                    }
                }

                outputStream.flush();
                Log.d(TAG, "file downloaded: " + fileSizeDownloaded + " of " + fileSize);
                if (callBack != null) {
                    handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onSucess(path, name, fileSize);

                        }
                    });
                    Log.d(TAG, "file downloaded: " + fileSizeDownloaded + " of " + fileSize);
                }

                return true;
            } catch (IOException e) {
                if (callBack != null) {
                    callBack.onError(e);
                }
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            if (callBack != null) {
                callBack.onError(e);
            }
            return false;
        }
    }
}
