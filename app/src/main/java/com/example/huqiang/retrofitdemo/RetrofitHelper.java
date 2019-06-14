package com.example.huqiang.retrofitdemo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.zhpan.idea.cookie.CookieJarImpl;
import com.zhpan.idea.cookie.PersistentCookieStore;
import com.zhpan.idea.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by 83916 on 2018/1/1.
 */

public class RetrofitHelper {

    private volatile static Retrofit retrofitInstance = null;

    /**
     * 创建Retrofit请求Api
     * @param clazz   Retrofit Api接口
     * @return api实例
     */
    public static <T> T createApi(Class<T> clazz){
        return getInstance().create(clazz);
    }

    // ===============================================================
    // private methods =================================================
    /**
     * 获取Retrofit实例
     * @return Retrofit
     */
    private static Retrofit getInstance(){
        if(null == retrofitInstance){
            synchronized (Retrofit.class){
                if(null == retrofitInstance){ // 双重检验锁,仅第一次调用时实例化
                    retrofitInstance = new Retrofit.Builder()
                            // baseUrl总是以/结束，@URL不要以/开头
                            .baseUrl(BuildConfig.API_SERVER_URL)
                            // 使用OkHttp Client
                            .client(buildOKHttpClient())
                            // 集成RxJava处理
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            // 集成Gson转换器
                            .addConverterFactory(buildGsonConverterFactory())
                            .build();
                }
            }
        }
        return retrofitInstance;
    }


    /**
     * 构建OkHttpClient
     * @return OkHttpClient
     */
    private static OkHttpClient buildOKHttpClient(){
        // 添加日志拦截器，非debug模式不打印任何日志
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(AppUtil.isDev() ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE) ;

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)                       // 添加日志拦截器
//                .addInterceptor(HttpHeaderInterceptor())                   //添加公共头
                .cookieJar(new CookieJarImpl(new PersistentCookieStore(Utils.getContext())))     //持久化管理你的Cookie
                //.addInterceptor(buildTokenInterceptor())                // 添加token拦截器
                .addNetworkInterceptor(buildCacheInterceptor())           // 添加网络缓存拦截器
                .cache(getCache())                                        // 设置缓存文件
                .retryOnConnectionFailure(true)                           // 自动重连
                .connectionSpecs(Arrays.asList(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))      //支持https,明文Http与比较新的Https
                .connectTimeout(15, TimeUnit.SECONDS)                     // 15秒连接超时
                .readTimeout(20, TimeUnit.SECONDS)                        // 20秒读取超时
                .writeTimeout(20, TimeUnit.SECONDS)                       // 20秒写入超时
                .build();
    }

    /**
     * 获取缓存对象
     * @return Cache
     */
    private static Cache getCache(){
        // 获取缓存目标,SD卡
        File cacheFile = new File(AppUtil.getContext().getCacheDir(), AppUtil.getContext().getResources().getString(R.string.app_name));
        // 创建缓存对象,最大缓存50m
        return new Cache(cacheFile, 1024*1024*20);
    }

    /**
     * 网络请求公共头信息插入器
     *@return Interceptor
     */
    public static Interceptor HttpHeaderInterceptor(){
      return new Interceptor() {
          @Override
          public Response intercept(Chain chain) throws IOException {
              Request original = chain.request();
              Request request = original.newBuilder()
                      .header("User-Agent", "Android, xxx")
                      .header("Accept", "application/json")
                      .header("Content-type", "application/json")
                      .method(original.method(), original.body())
                      .build();
              return chain.proceed(request);
          }
      };
    }

    /**
     * 构建缓存拦截器
     * @return Interceptor
     */
    private static Interceptor buildCacheInterceptor(){
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                // 无网络连接时请求从缓存中读取
                if (!AppUtil.isNetworkConnected()) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }

                // 响应内容处理
                // 在线时缓存5分钟
                // 离线时缓存4周
                Response response = chain.proceed(request);
                if (AppUtil.isNetworkConnected()) {
                    int maxAge = 300;
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")// 清除头信息，因为服务器如果不支持，会返回一些干扰信息，不清除下面无法生效
                            .build();
                }else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
    }

    /**
     * 构建GSON转换器
     * @return GsonConverterFactory
     */
    private static GsonConverterFactory buildGsonConverterFactory(){
        GsonBuilder builder = new GsonBuilder();
        builder.setLenient();

        // 注册类型转换适配器
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return null == json ? null : new Date(json.getAsLong());
            }
        });

        Gson gson = builder.create();
        return GsonConverterFactory.create(gson);
    }

    /**
     * 公共参数，有些接口某些参数是公共的，不可能一个个都去加
     * 参考链接  https://juejin.im/entry/5825300b2f301e005c47fac5
     */
    private Interceptor addQueryParamterInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (request.method().equals("GET")) {
                HttpUrl httpUrl = request.url().newBuilder()
                        .addQueryParameter("version", "xxx")
                        .addQueryParameter("device", "Android")
                        .addQueryParameter("timestamp", String.valueOf(System.currentTimeMillis()))
                        .build();
                request = request.newBuilder().url(httpUrl).build();
            } else if (request.method().equals("POST")) {
                if (request.body() instanceof FormBody) {
                    FormBody.Builder bodyBuilder = new FormBody.Builder();
                    FormBody formBody = (FormBody) request.body();

                    for (int i = 0; i < formBody.size(); i++) {
                        bodyBuilder.addEncoded(formBody.encodedName(i), formBody.encodedValue(i));
                    }
                    formBody = bodyBuilder
                            .addEncoded("version", "xxx")
                            .addEncoded("device", "Android")
                            .addEncoded("timestamp", String.valueOf(System.currentTimeMillis()))
                            .build();

                    request = request.newBuilder().post(formBody).build();
                }
            }

            return chain.proceed(request);
        }

    };
}
