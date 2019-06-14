package com.example.huqiang.retrofitdemo;

/**
 * Created by 83916 on 2018/1/1.
 */

public class HttpUtils extends RetrofitHelper {
    private static HttpUtils mHttpUtil;

    public HttpUtils() {
    }

    public static HttpUtils getInstance(){
        if (mHttpUtil==null){
            mHttpUtil = new HttpUtils();
        }
        return mHttpUtil;
    }
    /**
     * 管理多个模块的ApiService
     */
    public  MyApiService apiService = createApi(MyApiService.class);

    public  TestApiService testApi = createApi(TestApiService.class);

}
