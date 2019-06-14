package com.example.huqiang.retrofitdemo;


import com.zhpan.idea.net.common.Constants;
import com.zhpan.idea.net.common.IdeaApi;

public class NetUtils {
    private static MyApiService mIdeaApiService;
    private static TestApiService mTestApiService;

    public static MyApiService getApiService() {
        if (mIdeaApiService == null)
            mIdeaApiService = IdeaApi.getApiService(MyApiService.class, BuildConfig.API_SERVER_URL);
        return mIdeaApiService;
    }
    public static TestApiService getTestService(){
        if (mTestApiService ==null){
            mTestApiService = IdeaApi.getApiService(TestApiService.class, BuildConfig.API_SERVER_URL);
        }
        return mTestApiService;
    }
}
